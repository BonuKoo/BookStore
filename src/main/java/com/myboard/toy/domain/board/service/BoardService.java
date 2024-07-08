package com.myboard.toy.domain.board.service;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import com.myboard.toy.domain.board.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    //리스트
    public Page<BoardPageDTO> searchWithPage(BoardSearchCondition condition, Pageable pageable){
        return boardRepository.searchWithPage(condition, pageable);
    }
    //단 건 조회
    public BoardDTO getDetailBoardByIdWithReply(Long id){
        Board board = boardRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " +id));

        return BoardDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .replies(board.getReplies())
                .build();
    }
    //수정
    public BoardDTO modifyBoard(Long id, String title, String content){

        Board board = boardRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("해당하는 게시글을 찾을 수 없습니다. ID:"+id));

        board.update(title,content);
        Board modifiedBoard = boardRepository.save(board);

        return new BoardDTO(modifiedBoard.getId(), modifiedBoard.getTitle(), modifiedBoard.getContent());
    }

    //삭제
    public void removeBoard(Long id){
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 게시글을 찾을 수 없습니다. ID:" + id));

        boardRepository.delete(board);
    }

    //생성
    public BoardDTO createBoard(BoardDTO boardDTO){
        Board board = new Board(boardDTO.getTitle(), boardDTO.getContent());
        Board savedBoard = boardRepository.save(board);
        return new BoardDTO(savedBoard.getId(), savedBoard.getTitle(), savedBoard.getContent());
    }
}
