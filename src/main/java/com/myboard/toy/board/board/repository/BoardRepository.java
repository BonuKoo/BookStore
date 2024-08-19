package com.myboard.toy.board.board.repository;

import com.myboard.toy.board.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepository4QueryDSl {

}
