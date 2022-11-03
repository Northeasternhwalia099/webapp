package com.csye.webapp.controllers;

import com.csye.webapp.model.User;
import com.csye.webapp.model.Document;
import com.csye.webapp.repository.DocRepository;
import com.csye.webapp.repository.UserRepository;
import com.csye.webapp.service.S3BucketService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Transactional
@RestController
public class S3Controller {

    @Autowired
    private S3BucketService s3Service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocRepository documentRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private final static Logger LOG = LoggerFactory.getLogger(UserController.class);

    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    @PostMapping("/v1/documents")
    public ResponseEntity<?> uploadFiles(@RequestParam("file") MultipartFile file, HttpServletRequest request,
            HttpServletResponse response) {
        LOG.info("Inside upload file");
        long start = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        String authorization = request.getHeader("Authorization");
        JsonObject responseEntity = new JsonObject();
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            authorization = authorization.replaceFirst("Basic ", "");
            LOG.info("Inside basic auth");
            String credentials = new String(Base64.getDecoder().decode(authorization.getBytes()));

            String[] userCredentials = credentials.split(":", 2);
            String email = userCredentials[0];

            String password = userCredentials[1];

            User user = userRepository.findByemail(email);
            if (user == null) {
                long end = System.currentTimeMillis();
                LOG.error("User doesn't exist");
                responseEntity.addProperty("message", "User doesn't exist");
            } else if (user != null && !bCryptPasswordEncoder.matches(password, user.getPassword())) {
                long end = System.currentTimeMillis();
                LOG.error("Invalid password");
                responseEntity.addProperty("message", "Invalid password");
            } else if (!(documentRepository.findByName(file.getOriginalFilename()).isEmpty())) {
                LOG.error("File already uploaded");
                responseEntity.addProperty("message", "File already exists");
                return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.BAD_REQUEST);
            } else {
                String path = s3Service.saveFile(file, user.getId());

                // user.setPassword(null);
                long end = System.currentTimeMillis();
                LOG.info("User created wit time: " + (end - start));
                LOG.info("user authenticated");
                String dateFormat = simpleDateFormat.format(new Date());
                Document document = new Document();
                String user_id = user.getId();
                document.setUserid(user_id); // (user_id);
                document.setName(file.getOriginalFilename());
                // document.setName("MyFile");
                document.setDate_created(dateFormat);
                document.setS3_bucket_path(path);

                documentRepository.save(document);

                return new ResponseEntity<Document>(document, HttpStatus.OK);
            }

            return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.UNAUTHORIZED);

        }

        responseEntity.addProperty("message", "Invalid. Unable to Authenticate");
        long end = System.currentTimeMillis();
        // statsDClient.recordExecutionTime("getUserApiTime", (end-start));
        LOG.error("Invalid. Unable to Authenticate");
        return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/v1/documents/{id}")
    public ResponseEntity<?> getDocument(@PathVariable String id, HttpServletRequest request,
            HttpServletResponse response) {
        LOG.info("Inside getDocument() method");
        long start = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        String authorization = request.getHeader("Authorization");
        JsonObject responseEntity = new JsonObject();
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            authorization = authorization.replaceFirst("Basic ", "");
            LOG.info("Inside basic auth");
            String credentials = new String(Base64.getDecoder().decode(authorization.getBytes()));

            String[] userCredentials = credentials.split(":", 2);
            String email = userCredentials[0];

            String password = userCredentials[1];

            User user = userRepository.findByemail(email);
            if (user == null) {
                long end = System.currentTimeMillis();
                LOG.error("User doesn't exist");
                responseEntity.addProperty("message", "User doesn't exist");
            } else if (user != null && !bCryptPasswordEncoder.matches(password, user.getPassword())) {
                long end = System.currentTimeMillis();
                LOG.error("Invalid password");
                responseEntity.addProperty("message", "Invalid password");
            } else {

                String user_id = user.getId();
                Document document = documentRepository.findById(id);

                LOG.info("id: " + id);
                if (document == null) {
                    LOG.error("Document doesn't exist");
                    responseEntity.addProperty("message", "Document doesn't exist");
                    return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.BAD_REQUEST);
                } else {
                    return new ResponseEntity<Document>(document, HttpStatus.OK);
                }
            }
        }

        return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.UNAUTHORIZED);

    }

    @DeleteMapping("/v1/documents/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable String id, HttpServletRequest request,
            HttpServletResponse response) {
        HttpHeaders responseHeaders = new HttpHeaders();
        String authorization = request.getHeader("Authorization");
        JsonObject responseEntity = new JsonObject();
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            authorization = authorization.replaceFirst("Basic ", "");
            LOG.info("Inside basic auth");
            String credentials = new String(Base64.getDecoder().decode(authorization.getBytes()));

            String[] userCredentials = credentials.split(":", 2);
            String email = userCredentials[0];

            String password = userCredentials[1];

            User user = userRepository.findByemail(email);
            if (user == null) {
                long end = System.currentTimeMillis();
                LOG.error("User doesn't exist");
                responseEntity.addProperty("message", "User doesn't exist");
            } else if (user != null && !bCryptPasswordEncoder.matches(password, user.getPassword())) {
                long end = System.currentTimeMillis();
                LOG.error("Invalid password");
                responseEntity.addProperty("message", "Invalid password");
            } else {
                Document document = documentRepository.findById(id);
                if (document == null) {
                    LOG.error("Document doesn't exist");
                    // documentRepository.deleteById(id);
                    responseEntity.addProperty("message", "Document doesn't exist");

                    return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.BAD_REQUEST);
                } else {
                    LOG.info("inside delete document");

                    String output = s3Service.deleteFile(document.getName(), user.getId());
                    LOG.info("file deleted: " + output);
                    LOG.info("Document deleted from the bucket");
                    documentRepository.deleteById(id);
                    LOG.info("Document successfully deleted");
                    return new ResponseEntity<String>("DOCUMENT DELETED", HttpStatus.OK);
                }

            }
        }

        return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/v1/documents")
    public ResponseEntity<?> getAllDocuments(HttpServletRequest request, HttpServletResponse response) {
        LOG.info("Inside upload file");
        long start = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        String authorization = request.getHeader("Authorization");
        JsonObject responseEntity = new JsonObject();
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            authorization = authorization.replaceFirst("Basic ", "");
            LOG.info("Inside basic auth");
            String credentials = new String(Base64.getDecoder().decode(authorization.getBytes()));

            String[] userCredentials = credentials.split(":", 2);
            String email = userCredentials[0];

            String password = userCredentials[1];

            User user = userRepository.findByemail(email);
            if (user == null) {
                long end = System.currentTimeMillis();
                LOG.error("User doesn't exist");
                responseEntity.addProperty("message", "User doesn't exist");
            } else if (user != null && !bCryptPasswordEncoder.matches(password, user.getPassword())) {
                long end = System.currentTimeMillis();
                LOG.error("Invalid password");
                responseEntity.addProperty("message", "Invalid password");
            } else {

                String user_id = user.getId();

                LOG.info("Inside get documents ");
                LOG.info("user authenticated");
                List<Document> documents = documentRepository.findAllByUserid(user_id);

                return new ResponseEntity<>(documents, HttpStatus.OK);
            }

            return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.UNAUTHORIZED);

        }

        responseEntity.addProperty("message", "Invalid. Unable to Authenticate");
        long end = System.currentTimeMillis();
        // statsDClient.recordExecutionTime("getUserApiTime", (end-start));
        LOG.error("Invalid. Unable to Authenticate");
        return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.UNAUTHORIZED);
    }

}