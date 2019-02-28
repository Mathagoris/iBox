import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleDrive implements ICloudFilesystem {
    private static String CREDS_PATH;
    private static final String APPLICATION_NAME = "iBox-App";
    private static final String TOKENS_DIRECTORY_PATH = "/home/mathius/Documents/CS5850/tokens";
    private static final String DRIVE_FOLDER_ID_FILE = "/home/mathius/Documents/CS5850/drive-folder-id.txt";
    private static String DRIVE_FOLDER_ID;

    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Drive drive;

    @Override
    public boolean connect(String credsPath) {
        try {
            CREDS_PATH = credsPath;
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // authorization
            Credential credential = getCredentials();
            // set up the global Drive instance
            drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                    APPLICATION_NAME).build();
        } catch (IOException e) {
            System.err.println("Unable to connect to Google Drive.");
            e.printStackTrace(System.err);
            return false;
        } catch (GeneralSecurityException e) {
            System.err.println("Unable to establish HTTP Transport.");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }



    public boolean createNewFolder(String folderName) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(DRIVE_FOLDER_ID_FILE)));
            if (content.length() == 0) {
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(folderName);
                fileMetadata.setMimeType("application/vnd.google-apps.folder");

                com.google.api.services.drive.model.File file = drive.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                DRIVE_FOLDER_ID = file.getId();
                Files.write(Paths.get(DRIVE_FOLDER_ID_FILE), DRIVE_FOLDER_ID.getBytes(), StandardOpenOption.WRITE);
            } else {
                DRIVE_FOLDER_ID = content;
            }
        } catch (IOException e) {
            System.err.println("Unable to create folder on Google Drive.");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    private Credential getCredentials() throws IOException {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(iBox.class.getResourceAsStream(CREDS_PATH)));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(DriveScopes.DRIVE_FILE))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    @Override
    public boolean uploadFile(String parentDir, String fileToUpload) {
        try {
            java.io.File uploadFile = new java.io.File(parentDir + "/" + fileToUpload);
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(uploadFile.getName());

            fileMetadata.setParents(Collections.singletonList(DRIVE_FOLDER_ID));

            FileContent mediaContent = new FileContent(
                    URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile);
            drive.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
        } catch (IOException e){
            System.err.println("Unable to create drive file.");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateFile(String parentDir, String fileToUpdate) {
        try {
            java.io.File updateFile = new java.io.File(parentDir + "/" + fileToUpdate);
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(updateFile.getName());

            FileList driveFiles = drive.files().list().setQ("name='" + fileToUpdate + "'")
                    .setFields("files(id, name)").execute();
            if (driveFiles.size() == 0) {
                throw new IOException("Unable to update drive file because it doesn't exist");
            }
            String id = driveFiles.getFiles().get(0).getId();

            FileContent mediaContent = new FileContent(
                    URLConnection.guessContentTypeFromName(updateFile.getName()), updateFile);
            drive.files().update(id, fileMetadata, mediaContent).execute();
        } catch (IOException e) {
            System.err.println("Unable to update drive file. It may not exist.");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteFile(String fileToDelete) {
        try {
            FileList driveFiles = drive.files().list().setQ("name='" + fileToDelete + "'")
                    .setFields("files(id, name)").execute();
            if (driveFiles.size() == 0) {
                throw new IOException("Unable to delete drive file because it doesn't exist");
            }
            String id = driveFiles.getFiles().get(0).getId();
            drive.files().delete(id).execute();
        } catch (IOException e) {
            System.err.println("Unable to delete drive file. It may not exist.");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }
}
