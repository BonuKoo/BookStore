package com.myboard.toy.domain.reply;

import com.myboard.toy.domain.board.Board;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Reply {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    public Reply(String content){
        this.content = content;
    }

    public void assignBoard(Board board) {
        this.board = board;
    }
}
