package com.myboard.toy.domain.board.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Data;

@Data
public class BoardPageDTO {
    private Long id;
    private String title;
    private Long replyCount;

    @QueryProjection
    public BoardPageDTO(Long id, String title, Long replyCount) {
        this.id = id;
        this.title = title;
        this.replyCount = replyCount;
    }

}
