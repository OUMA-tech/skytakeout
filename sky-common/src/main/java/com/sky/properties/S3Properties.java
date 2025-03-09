package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "sky.aws.s3")
@Data
public class S3Properties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;
}
