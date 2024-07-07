package com.myboard.toy.domain.board.repository;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.reply.Reply;
import com.myboard.toy.domain.reply.repository.ReplyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class BoardRepositoryTest {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ReplyRepository replyRepository;

    @Test
    public void Create1BoardAnd3RepliesTest(){
        //given

        Board board = new Board();

        //when

        //then
    }
}