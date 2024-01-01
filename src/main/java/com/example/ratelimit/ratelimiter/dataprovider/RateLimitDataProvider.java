package com.example.ratelimit.ratelimiter.dataprovider;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitDataProvider {

    private final AmazonS3 s3;
    private final ObjectMapper mapper;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${cloud.aws.s3.rate-limit-config-key}")
    private String objectKey;


    @Bean
    public RateLimitData getRateLimitData() {
        log.info("loading s3 object...");
        S3Object s3Object = s3.getObject(bucketName, objectKey);
        try {
            return mapper.readValue(s3Object.getObjectContent(), RateLimitData.class);
        } catch (IOException e) {
            log.error("error reading s3 object", e);
            throw new RuntimeException(e);
        }
    }

}
