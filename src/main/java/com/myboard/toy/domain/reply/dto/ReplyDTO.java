package com.myboard.toy.domain.reply.dto;

import lombok.Builder;

@Builder
public class ReplyDTO {

    private Long id;
    private String content;
    private Long boardId;

}
