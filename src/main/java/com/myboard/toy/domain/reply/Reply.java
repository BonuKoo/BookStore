package com.myboard.toy.domain.reply;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.securityproject.domain.entity.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor
@Entity
public class Reply {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "reply_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "board_id")
    private Board board;

    //수정용
    public Reply(String content){
        this.content = content;
    }

    @Builder
    public Reply(Long id, String content, Board board, Account account) {
        this.id = id;
        this.content = content;
        this.board = board;
        this.account = account;
    }

    // 댓글 수정용
    public void updateContent(String content){
        this.content=content;
    }

    // Board를 함께 설정하는 예시
    public void updateContent(String content, Board board) {
        this.content = content;
        this.board = board;
    }

    public void assignBoard(Board board) {
        this.board = board;
    }
}
