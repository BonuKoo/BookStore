package com.myboard.toy.domain.file;

import com.myboard.toy.domain.board.Board;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
@Getter
@AllArgsConstructor @Builder
@Entity
 */
public class File {

    /*
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //이름
    @Column(nullable = false)
    private String fileName;

    //경로
    @Column(nullable = false)
    private String filePath;

    //타입
    @Column(nullable = false)
    private String fileType;

    //크기
    @Column(nullable = false)
    private long fileSize;

    //Board와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;


    public void attachFile(Board board){
        this.board=board;
    }
     */

}
