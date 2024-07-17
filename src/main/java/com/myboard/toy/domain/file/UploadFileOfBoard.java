package com.myboard.toy.domain.file;

import com.myboard.toy.domain.board.Board;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@Table(name = "UploadFile_Board")
@Entity
public class UploadFileOfBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uploadFileName; //고객이 업로드한 파일명
    private String storeFileName;  //서버 내부에서 관리하는 파일명 -> uuid

    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;

    public UploadFileOfBoard(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }
}