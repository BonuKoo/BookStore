package com.myboard.toy.domain.board.dto;

import com.myboard.toy.domain.reply.Reply;
import com.myboard.toy.domain.reply.dto.ReplyDTO;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.util.List;

@Data @NoArgsConstructor
@Builder
public class BoardDTO {
    private Long id;
    private String title;
    private String content;

    private List<Reply> replies;

    public BoardDTO(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    @QueryProjection
    public BoardDTO(Long id, String title, String content, List<Reply> replies) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.replies = replies;
    }
    
    //게시글 생성용
    public BoardDTO (String title,String content){
        this.title=title;
        this.content=content;
    }
}
