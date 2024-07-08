package com.myboard.toy.domain.board.service;

import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public List<Board> getAllBoards(){
        return boardRepository.findAll();
    }

    public Optional<Board> getById(Long id){
        return boardRepository.findById(id);
    }

    public Board save(Board board){
        return boardRepository.save(board);
    }

    public void deleteById(Long id){
        boardRepository.deleteById(id);
    }

}
