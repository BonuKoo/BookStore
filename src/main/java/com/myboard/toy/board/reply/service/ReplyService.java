package com.myboard.toy.board.reply.service;

import com.myboard.toy.board.board.repository.BoardRepository;
import com.myboard.toy.board.domain.Board;
import com.myboard.toy.board.domain.Reply;
import com.myboard.toy.board.domain.dto.ReplyDTO;
import com.myboard.toy.board.reply.repository.ReplyRepository;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;


    /*
        Create
     */

    public ReplyDTO createReply(Long boardId, String content, AccountDto accountId) {
        Long acId = accountId.getId();

        Optional<Account> accountOpt = userRepository.findById(acId);
        Account account = accountOpt.orElseThrow();

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + boardId));

        // Reply 엔티티 생성 및 저장

        Reply reply = Reply.builder()
                .content(content)
                .board(board)
                .account(account)
                .build();

        return toReplyDTO(replyRepository.save(reply));

    }

    /*
        UPDATE
     */

    public ReplyDTO updateReply(Long replyId,String content) {

        // 기존의 Reply 엔티티 조회
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + replyId));

        // 수정할 내용 업데이트
        reply.updateContent(content);

        return toReplyDTO(replyRepository.save(reply));
    }

    /*
        Delete
     */

    // 댓글 삭제
    @Transactional
    public void deleteReply(Long id) {
        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 댓글을 찾을 수 없습니다. ID: " + id));

        replyRepository.delete(reply);
    }

    private ReplyDTO toReplyDTO(Reply reply) {
        return ReplyDTO.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .boardId(reply.getBoard().getId())
                .account(reply.getAccount())
                .build();
    }
}
