package com.sky.config;

import com.sky.utils.S3Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
public class S3Config {

    @Value("${sky.aws.s3.region}")
    private String regionName;

    @Value("${sky.aws.s3.bucket-name}")
    private String bucketName;

    @Bean
    public S3Util s3Util() {
        return new S3Util(Region.of(regionName), bucketName);
    }
}
