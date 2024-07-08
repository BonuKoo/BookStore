package com.myboard.toy.domain.board.service;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.board.repository.BoardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class BoardServiceTest {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BoardService boardService;


    @Test
    void 게시글_단건조회(){
        //Boolean
        Optional<Board> byId = boardRepository.findById(1L);

        Board board = byId.get();

        BoardDTO boardDTO = boardService.getDetailBoardByIdWithReply(1L);

        //assertNotNull(boardDTO);
        //assertEquals(board.getId(),boardDTO.getId());
        assertEquals(board.getReplies(),boardDTO.getReplies());
    }
}