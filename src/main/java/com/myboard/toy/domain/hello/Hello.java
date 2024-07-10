package com.myboard.toy.domain.hello;

import com.myboard.toy.domain.hello.file.UploadFile;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Hello {

    private Long id;
    private String helloName;
    private UploadFile attachFile;
    private List<UploadFile> imageFiles;

    public Hello(String helloName, UploadFile attachFile, List<UploadFile> imageFiles) {
        this.helloName = helloName;
        this.attachFile = attachFile;
        this.imageFiles = imageFiles;
    }
}

    /*
        실험용 Domain Hello
    */