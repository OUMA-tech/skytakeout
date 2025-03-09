package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.S3Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "common api")
@Slf4j
@RequiredArgsConstructor
public class CommonController {

    private final S3Util s3Util;


    @PostMapping("/upload")
    @ApiOperation("upload file")
    public Result<String> upload(MultipartFile file){
        log.info("upload file:{}", file);

        try{
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            String originalFileName = file.getOriginalFilename();
            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            String s3key = "uploads/" + UUID.randomUUID() + suffix;
            String fileUrl = s3Util.uploadFile(tempFile.getAbsolutePath(), s3key);
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("upload file failed: {}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
