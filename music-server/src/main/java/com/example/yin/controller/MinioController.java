package com.example.yin.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.InputStream;


@Controller
public class MinioController {
    @Value("${minio.bucket-name}")
    private String bucketName;
    @Autowired
    private MinioClient minioClient;


    @GetMapping("/img/backgroundImg/{fileName:.+}")
    public ResponseEntity<byte[]> getRankImage(@PathVariable String fileName) throws Exception {
        // 桶名 bucketName 比如 user01
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object("img/backgroundImg/"+fileName) // 这里不要重复加路径前缀
                        .build()
        );
        byte[] bytes = IOUtils.toByteArray(stream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    // 获取歌曲
    @GetMapping("/song/{fileName:.+}")
    public ResponseEntity<byte[]> getMusic(@PathVariable String fileName) {
        try {
            // MinIO 实际路径：user01/song/xxx.mp3
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object("song/" + fileName)
                    .build();

            InputStream inputStream = minioClient.getObject(args);
            byte[] bytes = IOUtils.toByteArray(inputStream);

            HttpHeaders headers = new HttpHeaders();

            // 根据文件后缀设置 Content-Type
            if (fileName.toLowerCase().endsWith(".mp3")) {
                headers.setContentType(MediaType.valueOf("audio/mpeg"));
            } else if (fileName.toLowerCase().endsWith(".m4a")) {
                headers.setContentType(MediaType.valueOf("audio/mp4")); // m4a 属于 mp4 容器
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //获取歌手图片
    @GetMapping("/img/singerPic/{fileName:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object("img/singerPic/"+fileName)
                        .build()
        );

        byte[] bytes = IOUtils.toByteArray(stream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // 设置响应内容类型为图片类型，根据实际情况修改

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }


// 获取歌单图片
@GetMapping("/img/songListPic/{fileName:.+}")
public ResponseEntity<byte[]> getSongListImage(@PathVariable String fileName) throws Exception {
    InputStream stream = minioClient.getObject(
            GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object("img/songListPic/" + fileName) // 对应 MinIO 中的实际路径
                    .build()
    );

    byte[] bytes = IOUtils.toByteArray(stream);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG); // 如果图片可能有 png，可以改成 MediaType.IMAGE_JPEG_VALUE 或动态判断

    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
}


    //获取歌的图片
    @GetMapping("/img/songPic/{fileName:.+}")
    public ResponseEntity<byte[]> getImage2(@PathVariable String fileName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object("img/songPic/"+fileName)
                        .build()
        );

        byte[] bytes = IOUtils.toByteArray(stream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // 设置响应内容类型为图片类型，根据实际情况修改

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
    //获取头像
    ///img/avatorImages/
    @GetMapping("/img/avatorImages/{fileName:.+}")
    public ResponseEntity<byte[]> getImage3(@PathVariable String fileName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object("img/avatorImages/"+fileName)
                        .build()
        );

        byte[] bytes = IOUtils.toByteArray(stream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // 设置响应内容类型为图片类型，根据实际情况修改

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
