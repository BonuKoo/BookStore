package com.myboard.toy.infrastructure.board.repository;

import com.myboard.toy.domain.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>,BoardRepository4QueryDSl {



}
