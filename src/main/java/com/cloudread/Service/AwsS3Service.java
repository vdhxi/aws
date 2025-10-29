package com.cloudread.Service;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;

public interface AwsS3Service {
    String uploadFile(MultipartFile file) throws IOException;

    String uploadFileWithCustomPrefix(MultipartFile file, String prefix) throws IOException;

    byte[] downloadFile(String filename) throws IOException;

    void deleteFile(String filename) throws IOException;

    String getPublicUrl(String filename);

    boolean fileExists(String filename);

    GetObjectResponse getFileMetadata(String filename);
}


