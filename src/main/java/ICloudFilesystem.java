import java.io.File;

public interface ICloudFilesystem {
    boolean connect(String credsPath);
    boolean createNewFolder(String folderName);
    boolean uploadFile(String parentDir, String fileToUpload);
    boolean updateFile(String parentDir, String fileToUpdate);
    boolean deleteFile(String fileToDelete);
}
