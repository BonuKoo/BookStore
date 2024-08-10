package com.myboard.toy.board.domain.dto;

import com.myboard.toy.security.domain.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder @AllArgsConstructor
public class ReplyDTO {

    private Long id;
    private String content;
    private Long boardId;
    private Account account;


}
