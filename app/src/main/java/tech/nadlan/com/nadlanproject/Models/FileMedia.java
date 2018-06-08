package tech.nadlan.com.nadlanproject.Models;

/**
 * Created by מוחמד on 01/06/2018.
 */

public class FileMedia {
    private String fileName,path;

    public FileMedia(String fileName, String path) {
        this.fileName = fileName;
        this.path = path;
    }
    public FileMedia() {
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
