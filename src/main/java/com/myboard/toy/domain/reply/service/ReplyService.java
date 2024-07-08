package com.myboard.toy.domain.reply.service;

import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.reply.repository.ReplyRepository;
import org.springframework.stereotype.Service;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;

    public ReplyService(ReplyRepository replyRepository) {
        this.replyRepository = replyRepository;
    }

}
