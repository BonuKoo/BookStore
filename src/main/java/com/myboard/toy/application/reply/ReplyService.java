package com.myboard.toy.application.reply;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.infrastructure.board.BoardRepository;
import com.myboard.toy.domain.reply.Reply;
import com.myboard.toy.domain.reply.dto.ReplyDTO;
import com.myboard.toy.infrastructure.reply.ReplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;

    public ReplyService(ReplyRepository replyRepository, BoardRepository boardRepository) {
        this.replyRepository = replyRepository;
        this.boardRepository = boardRepository;
    }

    /*
        Create
     */
    public ReplyDTO createReply(ReplyDTO replyDTO) {
        // 게시글 ID로 해당하는 Board 엔티티 조회
        Board board = boardRepository.findById(replyDTO.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + replyDTO.getBoardId()));
        // Reply 엔티티 생성 및 저장
        Reply reply = new Reply(replyDTO.getContent(), board);
        Reply savedReply = replyRepository.save(reply);

        // ReplyDTO로 변환하여 반환
        return ReplyDTO.builder()
                .id(savedReply.getId())
                .content(savedReply.getContent())
                .boardId(savedReply.getBoard().getId())
                .build();
    }

    /*
        Read
     */
    public ReplyDTO getReplyById(Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + replyId));

        return ReplyDTO.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .boardId(reply.getBoard().getId())
                .build();
    }
    /*
        Update
     */

    // 댓글 수정


    public ReplyDTO updateReply(Long replyId, ReplyDTO replyDTO) {
        // 기존의 Reply 엔티티 조회
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + replyId));

        // 수정할 내용 업데이트
        reply = new Reply(reply.getId(),replyDTO.getContent(), reply.getBoard());

        // 변경된 Reply 엔티티 저장
        Reply updatedReply = replyRepository.save(reply);

        // 수정된 ReplyDTO로 변환하여 반환
        return ReplyDTO.builder()
                .id(updatedReply.getId())
                .content(updatedReply.getContent())
                .boardId(updatedReply.getBoard().getId())
                .build();
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
}
