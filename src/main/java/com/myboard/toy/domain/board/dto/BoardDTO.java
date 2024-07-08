package com.myboard.toy.domain.board.dto;

import com.myboard.toy.domain.reply.Reply;
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
    //ReplyDTO로 바꿔야 하나?
    private List<Reply> replies;

    @QueryProjection
    public BoardDTO(Long id, String title, String content, List<Reply> replies) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.replies = replies;
    }
}
