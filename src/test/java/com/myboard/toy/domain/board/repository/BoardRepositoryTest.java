package com.myboard.toy.domain.board.repository;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import com.myboard.toy.domain.reply.Reply;
import com.myboard.toy.domain.reply.repository.ReplyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
class BoardRepositoryTest {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ReplyRepository replyRepository;


    @BeforeEach
    void setUp(){
        // 데이터 초기화
        Board board1 = new Board("게시글1", "내용1");
        board1.addReply(new Reply("댓글1"));
        board1.addReply(new Reply("댓글2"));

        Board board2 = new Board("게시글2", "내용2");
        board2.addReply(new Reply("댓글3"));

        Board board3 = new Board("게시글3", "내용3");

        boardRepository.saveAll(Arrays.asList(board1, board2, board3));
    }

    @Test
    public void Create1BoardAnd3RepliesTest(){
        //given
        /*
            댓글이 3개
         */
        Reply reply1 = new Reply("댓글1");
        Reply reply2 = new Reply("댓글2");
        Reply reply3 = new Reply("댓글3");

        /*
            게시글 1개
         */
        Board board1 = new Board("게시글1","내용1");
        board1.addReply(reply1);
        board1.addReply(reply2);
        board1.addReply(reply3);

        //when
        //게시글 1
        boardRepository.save(board1);
        Optional<Board> boardFromDBOptinalValue = boardRepository.findById(1L);
        Board boardObject = boardFromDBOptinalValue.get();
        //댓글 1
        Reply firstReply = boardObject.getReplies().get(0);
        //then
        //제목 일치한가?
        //Assertions.assertThat(boardObject.getTitle()).isEqualTo(board1.getTitle());
        //댓글 개수 맞나?
        //Assertions.assertThat(boardObject.getReplies().size()).isEqualTo(3);
        assertThat(firstReply.getContent()).isEqualTo("댓글1"); //True
        //같은 board에 할당되어있나
        assertThat(firstReply.getBoard().getId()).isEqualTo(boardObject.getId());
    }

    @Test
    void searchWithPage_동작테스트(){
        //Given : condition
        BoardSearchCondition condition = new BoardSearchCondition();
        condition.setTitle("게시글");

        PageRequest pageable = PageRequest.of(0,10);

        //when
        Page<BoardPageDTO> result = boardRepository.searchWithPage(condition, pageable);

        //then
        List<BoardPageDTO> content = result.getContent();

        Assertions.assertThat(content).hasSize(3);
        Assertions.assertThat(content.get(0).getTitle()).isEqualTo("게시글1");
        Assertions.assertThat(content.get(0).getReplyCount()).isEqualTo(2);
        Assertions.assertThat(content.get(1).getTitle()).isEqualTo("게시글2");
        Assertions.assertThat(content.get(1).getReplyCount()).isEqualTo(1);
        Assertions.assertThat(content.get(2).getTitle()).isEqualTo("게시글3");
        Assertions.assertThat(content.get(2).getReplyCount()).isEqualTo(0);
    }
}