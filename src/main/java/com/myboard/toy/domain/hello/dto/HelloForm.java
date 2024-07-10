package com.myboard.toy.domain.hello.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Data
public class HelloForm {

    private Long helloId;
    private String helloName;
    private List<MultipartFile> imageFiles;
    private MultipartFile attachFiles;
}

/*
    imageFiles : 이미지 다중 업로드를 위해 List<MultipartFile>
    attachFiles : 멀티파트는
 */