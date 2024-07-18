package com.myboard.toy.domain.file.board;

import com.myboard.toy.domain.board.Board;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@Table(name = "UploadFile_Board")
@Entity
public class UploadFileOfBoard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uploadFileName; //고객이 업로드한 파일명
    private String storeFileName;  //서버 내부에서 관리하는 파일명 -> uuid

    /*경로
    @Column(nullable = false)
    private String filePath;
     */

    /*타입
    @Column(nullable = false)
    private String fileType;
     */

    /*
     크기
    @Column(nullable = false)
    private long fileSize;
    */

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "board_id")
    private Board board;

    //파일 이름
    public UploadFileOfBoard(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }
    //board와 연관관계 매서드
    public void attachBoard(Board board){
        this.board=board;
    }
}