package com.myboard.toy.board.domain.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class BoardPageDTO {
    private Long id;
    private String title;
    private String username;
    private Long replyCount;

    @QueryProjection
    public BoardPageDTO(Long id, String title,String username, Long replyCount) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.replyCount = replyCount;
    }

}
