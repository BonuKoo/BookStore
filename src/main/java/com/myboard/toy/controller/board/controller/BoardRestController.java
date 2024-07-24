package com.myboard.toy.controller.board.controller;

import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import com.myboard.toy.application.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BoardRestController {

    private final BoardService boardService;

    //@GetMapping("/boards")
    public Page<BoardPageDTO> getBoards(@RequestParam(required = false) String title,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        BoardSearchCondition condition = new BoardSearchCondition();
        condition.setTitle(title);

        Pageable pageable = PageRequest.of(page, size);

        return boardService.searchWithPage(condition, pageable);
    }
}
