package com.myboard.toy.domain.reply.dto;

import com.myboard.toy.securityproject.domain.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder @AllArgsConstructor
public class ReplyDTO {

    private Long id;
    private String content;
    private Long boardId;
    private Account account;

    public ReplyDTO() {
    }

    public ReplyDTO(String content, Long boardId) {
        this.content = content;
        this.boardId = boardId;
    }

}
