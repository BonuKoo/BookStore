package com.myboard.toy.domain.board.repository;

import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoardRepository4QueryDSl {
    Page<BoardPageDTO> searchWithPage(BoardSearchCondition condition, Pageable pageable);
}
