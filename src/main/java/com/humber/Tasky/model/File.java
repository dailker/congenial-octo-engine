package com.humber.Tasky.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "files")
public class File {

    @Id
    private String id;
    private String name;
    private String contentType;
    private long size;
    private byte[] content;
    private String uploaderId;
    private LocalDateTime uploadDate;

    public File() {
        this.uploadDate = LocalDateTime.now();
    }

    public File(String name, String contentType, byte[] content, String uploaderId) {
        this();
        this.name = name;
        this.contentType = contentType;
        this.content = content;
        this.uploaderId = uploaderId;
        this.size = content != null ? content.length : 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        this.size = content != null ? content.length : 0;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}