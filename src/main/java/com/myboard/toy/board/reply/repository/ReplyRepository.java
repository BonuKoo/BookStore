package com.myboard.toy.board.reply.repository;

import com.myboard.toy.board.domain.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply,Long> {
}
