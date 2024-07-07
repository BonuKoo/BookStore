package com.myboard.toy.domain.board.dto;

import com.myboard.toy.domain.reply.dto.ReplyDTO;
import lombok.*;

import java.util.List;

@Getter @Builder
public class BoardDTO {
    private final Long id;
    private final String title;
    private final String content;
    private final List<ReplyDTO> replies;
}
