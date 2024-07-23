package com.myboard.toy.domain.file;

import com.myboard.toy.domain.board.Board;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/*

*   Korea IT 수업 내용
    --- 게시판 전용 File 예제 ---

* */


//@Getter
//@AllArgsConstructor @Builder
//@Entity
public class File {


    //@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //이름
    //@Column(nullable = false)
    private String file_Name;

    //경로
    //@Column(nullable = false)
    private String file_Path;

    //파일 원래 이름
    //@Column(nullable = false)
    private String original_Name;

    //userId
    //private String loginId; - > User user; 연관관계 매핑

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "board_id")
    private Board board;

    //타입
    //@Column(nullable = false)
    private String fileType;

    //크기
    //@Column(nullable = false)
    private long fileSize;

    public void attachFile(Board board){
        this.board=board;
    }


}
