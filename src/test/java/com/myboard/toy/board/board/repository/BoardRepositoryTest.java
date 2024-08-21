package com.myboard.toy.board.board.repository;

import com.myboard.toy.board.domain.entity.Board;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BoardRepositoryTest {

    @Autowired
    BoardRepository boardRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    void createBoard(){

        Long accountId = 14L;
        Account account14L = userRepository.findById(accountId).get();

        for (int i = 1; i<3000; i++){
            Board board = Board.builder()
                    .title("제목" + i)
                    .content("내용" + i)
                    .account(account14L)
                    .build();
            boardRepository.save(board);
        }

    }
}