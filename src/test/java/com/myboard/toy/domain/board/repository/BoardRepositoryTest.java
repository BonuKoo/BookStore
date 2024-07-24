package com.myboard.toy.domain.board.repository;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.reply.Reply;
import com.myboard.toy.infrastructure.board.repository.BoardRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

//@DataJpaTest
//@Transactional
//@SpringBootTest
class BoardRepositoryTest {

    /*
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private EntityManager entityManager;
    */
    /*
    @BeforeEach
    void setUp() {
        List<Board> boards = new ArrayList<>();

        //게시글 2000개
        //각 게시글마다 댓글 6개
        for (int i = 1; i <= 1100; i++) {
            Board board = Board.builder()
                    .title("게시글 " + i)
                    .content("내용 " + i)
                    .build();

            for (int j = 1; j <= 6; j++) {
                Reply reply = new Reply("댓글 " + j + " for 게시글 " + i);
                board.addReply(reply);
            }

            boards.add(board);
        }

        boardRepository.saveAll(boards);
    }
    */

    /*
    @Test
    //@Transactional
    void testDummyDataInsertion() {
        // Given
        long boardCount = boardRepository.count();

        // Then
        assertThat(boardCount).isEqualTo(2000);
        Board board = boardRepository.findById(1L).orElseThrow();
        assertThat(board.getReplies().size()).isEqualTo(6);
    }
    */

    //@Test
    public void Create1BoardAnd3RepliesTest(){
        //given

    //        댓글이 3개

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
        /*
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
        */
    }


    /*
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
     */
    /*
    @Test
    void searchWithPage() {
        //given
        BoardSearchCondition condition = new BoardSearchCondition();
        condition.setTitle("게시글");

        PageRequest pageable = PageRequest.of(0, 10);

        //when
        Page<BoardPageDTO> result = boardRepository.searchWithPage(condition, pageable);

        //then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(110);
        List<BoardPageDTO> content = result.getContent();

        Assertions.assertThat(content).hasSize(10);
        Assertions.assertThat(content.get(0).getTitle()).isEqualTo("게시글1");
        Assertions.assertThat(content.get(0).getReplyCount()).isEqualTo(5);
        Assertions.assertThat(content.get(1).getTitle()).isEqualTo("게시글2");
        Assertions.assertThat(content.get(1).getReplyCount()).isEqualTo(15);
        Assertions.assertThat(content.get(2).getTitle()).isEqualTo("게시글3");
        Assertions.assertThat(content.get(2).getReplyCount()).isEqualTo(20);
        Assertions.assertThat(content.get(3).getTitle()).isEqualTo("게시글4");
        Assertions.assertThat(content.get(3).getReplyCount()).isEqualTo(0);

    }
     */


}