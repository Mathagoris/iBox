
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.InputStreamReader;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class iBox {
    private static String DIR_TO_WATCH;
    private static String CREDS_PATH;
    private static final String APPLICATION_NAME = "iBox-App";

    private static FileDataStoreFactory dataStoreFactory;
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Drive drive;

    public iBox(String dirToWatch, String credsPath) throws Exception {
        DIR_TO_WATCH = dirToWatch;
        CREDS_PATH = credsPath;
        connectToGoogleDrive();
    }

    private void connectToGoogleDrive() throws Exception{
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        // authorization
        Credential credential = getCredentials();
        // set up the global Drive instance
        drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                APPLICATION_NAME).build();
    }

    private static Credential getCredentials() throws IOException {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(iBox.class.getResourceAsStream(CREDS_PATH)));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static void watch() throws IOException {
        Path dir = Paths.get(DIR_TO_WATCH);
        WatchService watcher;
        WatchKey key;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            key = dir.register(watcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY);
        } catch (IOException x) {
            System.err.println(x);
            return;
        }
        for (;;) {
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // This key is registered only
                // for ENTRY_CREATE events,
                // but an OVERFLOW event can
                // occur regardless if events
                // are lost or discarded.
                if (kind == OVERFLOW) {
                    continue;
                }

                // The filename is the
                // context of the event.
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path filename = ev.context();

                if (kind == ENTRY_CREATE) {
                    uploadFile(filename);
                } else if (kind == ENTRY_MODIFY) {
                    updateFile(filename);
                } else if (kind == ENTRY_DELETE) {
                    deleteFile(filename);
                }
                System.out.println("***Directory changed: " + filename + "***");
                System.out.println("File action: " + kind);
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    private static File uploadFile(Path filename) throws IOException {
        java.io.File fileToUpload = filename.toFile();
        File fileMetadata = new File();
        fileMetadata.setTitle(fileToUpload.getName());

        FileContent mediaContent = new FileContent(
                URLConnection.guessContentTypeFromName(fileToUpload.getName()), fileToUpload);
        Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
        MediaHttpUploader uploader = insert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(true);
        return insert.execute();
    }

    private static File updateFile(Path filename) throws IOException{
        java.io.File fileToUpload = filename.toFile();
        File fileMetadata = new File();
        fileMetadata.setTitle(fileToUpload.getName());

        FileList driveFiles = drive.files().list().setQ("name='" + filename + "'")
                .setFields("files(id, name)").execute();
        if (driveFiles.size() == 0) {
            throw new IOException("Unable to update drive file because it doesn't exist");
        }
        String id = driveFiles.getItems().get(0).getId();

        FileContent mediaContent = new FileContent(
                URLConnection.guessContentTypeFromName(fileToUpload.getName()), fileToUpload);
        Drive.Files.Update update = drive.files().update(id, fileMetadata, mediaContent);
        return update.execute();
    }

    private static void deleteFile(Path filename) throws IOException{
        FileList driveFiles = drive.files().list().setQ("name='" + filename + "'")
                .setFields("files(id, name)").execute();
        if (driveFiles.size() == 0) {
            throw new IOException("Unable to delete drive file because it doesn't exist");
        }
        String id = driveFiles.getItems().get(0).getId();
        drive.files().delete(id).execute();
    }
}
