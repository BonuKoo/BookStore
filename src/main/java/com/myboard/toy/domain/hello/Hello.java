package com.myboard.toy.domain.hello;

import com.myboard.toy.domain.file.UploadFile;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Hello {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String helloName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attach_file_id")
    private UploadFile attachFile;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "hello_id") //외부키 생성
    private List<UploadFile> imageFiles = new ArrayList<>();

    public Hello(String helloName, UploadFile attachFile, List<UploadFile> imageFiles) {
        this.helloName = helloName;
        this.attachFile = attachFile;
        this.imageFiles = imageFiles;
    }
}
