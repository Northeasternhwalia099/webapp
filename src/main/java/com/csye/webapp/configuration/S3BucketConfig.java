package com.csye.webapp.configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;

@Configuration
public class S3BucketConfig {
    @Value("${region}")
    private String region_is;
    @Value("${secret}")
    private String secret_is;
    @Value("${accessKey}")
    private String access_Keyis;
    @Bean
    public AmazonS3 s3bucket() {

        AWSCredentials aws_creds = 
                new BasicAWSCredentials(access_Keyis, secret_is);
        return 
                AmazonS3ClientBuilder
                        .standard()
                        .withRegion(region_is)
                .withCredentials(new AWSStaticCredentialsProvider(aws_creds))
                        .build();

    }
}