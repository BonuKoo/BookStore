package com.myboard.toy.board.board.repository;

import com.myboard.toy.board.domain.dto.BoardPageDTO;
import com.myboard.toy.board.domain.dto.BoardSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardRepository4QueryDSl {
    Page<BoardPageDTO> searchWithPage(BoardSearchCondition condition, Pageable pageable);
}
