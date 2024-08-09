package com.myboard.toy.board.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder @AllArgsConstructor
public class ReplyDTO {

    private Long id;
    private String content;
    private Long boardId;
    private Long accountId;

    public ReplyDTO() {
    }

    public ReplyDTO(String content, Long boardId, Long accountId) {
        this.content = content;
        this.boardId = boardId;
        this.accountId = accountId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }
}
