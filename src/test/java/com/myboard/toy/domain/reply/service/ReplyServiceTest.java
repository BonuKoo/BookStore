package com.myboard.toy.domain.reply.service;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.board.dto.BoardDTO;
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

    @Test
    void testUpdateReply(){
        //Given

        //게시글, 댓글 생성
        Board board = new Board("게시글 제목","게시글 내용");
        Reply reply = new Reply("댓글1");

        board.addReply(reply);

        //저장
        Board savedBoard1 = boardRepository.save(board);
        Reply savedReply1 = replyRepository.save(reply);

        //When
        String updatedContent ="Updated Reply Content";

        ReplyDTO updatedReplyDTO = ReplyDTO.builder()
                .id(savedReply1.getId())
                .content(updatedContent)
                .boardId(savedBoard1.getId())
                .build();

        ReplyDTO updatedReply = replyService.updateReply(savedReply1.getId(), updatedReplyDTO);

        // Then: 업데이트된 내용을 검증
        assertThat(updatedReplyDTO).isNotNull();
        assertThat(updatedReplyDTO.getContent()).isEqualTo(updatedContent);
        assertThat(updatedReplyDTO.getBoardId()).isEqualTo(savedBoard1.getId());

        // 추가 검증: 실제 데이터베이스에 반영되었는지 확인
        Reply updatedReply2 = replyRepository.findById(savedReply1.getId()).orElseThrow();
        assertThat(updatedReply2.getContent()).isEqualTo(updatedContent);
    }
}