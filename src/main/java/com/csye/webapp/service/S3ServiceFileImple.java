package com.csye.webapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3ServiceFileImple {

    // String saveFile(MultipartFile file, String id);

    // byte[] downloadFile(String fileName);

    // public String deleteFile(String name);

    // List<String> listAllFiles();

    String saveFile(MultipartFile file, String userid);

    byte[] downloadFile(String fileName);

    public String deleteFile(String name, String user);

    List<String> listAllFiles();
}
