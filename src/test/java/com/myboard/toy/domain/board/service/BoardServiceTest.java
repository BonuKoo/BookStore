package com.myboard.toy.domain.board.service;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.board.repository.BoardRepository;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@Slf4j
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

    @Test
    void 게시글_단건수정(){

        //Given
        Long boardId = 1L;
        String newTitle = "게시글1 제목 수정";
        String newContent = "게시글1 내용 수정";

        //When
        BoardDTO modifiedBoard = boardService.modifyBoard(boardId, newTitle, newContent);
        //THEN - 수정
        Assertions.assertThat(modifiedBoard).isNotNull();
        Assertions.assertThat(modifiedBoard.getId()).isEqualTo(boardId);
        Assertions.assertThat(modifiedBoard.getTitle()).isEqualTo(newTitle);
        Assertions.assertThat(modifiedBoard.getContent()).isEqualTo(newContent);

        log.info("modifiedBoard id: {}",modifiedBoard.getId());
        log.info("modifiedBoard title: {}",modifiedBoard.getTitle());
        log.info("modifiedBoard content: {}",modifiedBoard.getContent());

    }
}