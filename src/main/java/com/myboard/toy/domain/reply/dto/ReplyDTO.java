package com.myboard.toy.domain.reply.dto;

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

    public ReplyDTO() {
    }

    public ReplyDTO(String content, Long boardId) {
        this.content = content;
        this.boardId = boardId;
    }
}
