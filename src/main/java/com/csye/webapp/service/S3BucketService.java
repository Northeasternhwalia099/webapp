package com.csye.webapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.io.ByteArrayInputStream;
import java.util.Date;

@Service
public class S3BucketService implements S3ServiceFileImple {

    private final AmazonS3 s3;

    @Value("${bucketName}")
    private String bucketName;

    public S3BucketService(AmazonS3 s3) {
        this.s3 = s3;
    }

    public String saveFile(MultipartFile file, String userid) {
        String fileName = file.getOriginalFilename();
        String folder = "folder";
        try {
            File convertedFile = convertMultiPartFile(file);
            PutObjectResult putObjectResult = s3.putObject(bucketName, userid + "/" + fileName, convertedFile);
            return " " + s3.getUrl(bucketName, userid + "/" + fileName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public byte[] downloadFile(String fileName) {
        S3Object object = s3.getObject(bucketName, fileName);
        S3ObjectInputStream objectInputStream = object.getObjectContent();
        try {
            return IOUtils.toByteArray(objectInputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);

        }
    }

    @Override
    public String deleteFile(String fileName, String userid) {
        s3.deleteObject(bucketName, userid + "/" + fileName);
        return "File deleted from s3";

    }

    @Override
    public List<String> listAllFiles() {

        ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(bucketName);
        return listObjectsV2Result.getObjectSummaries().stream().map(o -> o.getKey()).collect(Collectors.toList());
    }

    private File convertMultiPartFile(MultipartFile file) throws IOException {
        File mfile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(mfile);
        fos.write(file.getBytes());
        fos.close();
        return mfile;
    }
}
