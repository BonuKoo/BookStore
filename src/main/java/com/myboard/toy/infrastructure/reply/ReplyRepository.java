package com.myboard.toy.infrastructure.reply;

import com.myboard.toy.domain.reply.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply,Long> {
}
