package com.lab06.filemanagerservice.dto;

public class UploadResponse {

    private String message;
    private String fileUrl;
    private String fileName;

    public UploadResponse() {
    }

    public UploadResponse(String message, String fileUrl, String fileName) {
        this.message = message;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
