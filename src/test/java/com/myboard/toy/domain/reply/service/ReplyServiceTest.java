package com.myboard.toy.domain.reply.service;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.board.repository.BoardRepository;
import com.myboard.toy.domain.reply.Reply;
import com.myboard.toy.domain.reply.dto.ReplyDTO;
import com.myboard.toy.domain.reply.repository.ReplyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class ReplyServiceTest {

    @Autowired
    private ReplyService replyService;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private BoardRepository boardRepository;
    
    //생성 성공
    @Test
    void testCreateReply() {
        // Given
        Board board = new Board("게시글 제목", "게시글 내용");
        boardRepository.save(board);

        ReplyDTO replyDTO = ReplyDTO.builder()
                .content("댓글 내용")
                .boardId(board.getId())
                .build();

        // When
        ReplyDTO createdReplyDTO = replyService.createReply(replyDTO);

        // Then
        assertThat(createdReplyDTO).isNotNull();
        assertThat(createdReplyDTO.getId()).isNotNull();
        assertThat(createdReplyDTO.getContent()).isEqualTo(replyDTO.getContent());
        assertThat(createdReplyDTO.getBoardId()).isEqualTo(replyDTO.getBoardId());

        // Optional: 테스트 후 정리
        replyRepository.deleteById(createdReplyDTO.getId());
        boardRepository.deleteById(board.getId());
    }
    //TODO :: 댓글 수정
    
}