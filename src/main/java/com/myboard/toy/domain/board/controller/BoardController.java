package com.myboard.toy.domain.board.controller;

import com.myboard.toy.domain.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

}
