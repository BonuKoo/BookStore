package com.myboard.toy.domain.board.dto;

import com.myboard.toy.domain.reply.dto.ReplyDTO;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.util.List;

@Getter @Data
@Builder
public class BoardDTO {
    private Long id;
    private String title;
    private String content;
    private List<ReplyDTO> replies;

    @QueryProjection
    public BoardDTO(Long id, String title, String content, List<ReplyDTO> replies) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.replies = replies;
    }
}
