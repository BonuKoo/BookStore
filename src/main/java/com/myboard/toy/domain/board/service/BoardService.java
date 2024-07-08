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

    public Page<BoardPageDTO> searchWithPage(BoardSearchCondition condition, Pageable pageable){
        return boardRepository.searchWithPage(condition, pageable);
    }

    public BoardDTO getDetailBoardByIdWithReply(Long id){
        Board board = boardRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " +id));

        return  BoardDTO.builder()
                .id(board.getId())
                .title(board.getContents())
                .content(board.getContents())
                .replies(board.getReplies())
                .build();
    }
}
