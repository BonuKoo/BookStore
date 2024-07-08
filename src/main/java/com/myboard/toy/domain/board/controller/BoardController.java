package com.myboard.toy.domain.board.controller;

import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import com.myboard.toy.domain.board.service.BoardService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@Slf4j
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/boards")
    public String searchWithPage(@RequestParam(required = false) String title,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {

        BoardSearchCondition condition = new BoardSearchCondition();
        //검색 조건 설정
        condition.setTitle(title);

        Pageable pageable = PageRequest.of(page,size);

        Page<BoardPageDTO> boardPage = boardService.searchWithPage(condition, pageable);

        model.addAttribute("boards", boardPage);

        return "boardList";
    }

    @GetMapping("/boards/{id}")
    public String viewDetailBoardWithReplyList(@PathVariable Long id, Model model) {
        BoardDTO boardDTO = boardService.getDetailBoardByIdWithReply(id);

        model.addAttribute("board", boardDTO);
        return "boardDetail"; // 뷰 이름
    }

    @GetMapping("/boards/{id}/edit")
    public String editBoard(@PathVariable Long id, Model model) {
        BoardDTO boardDTO = boardService.getDetailBoardByIdWithReply(id);
        model.addAttribute("board", boardDTO);
        return "updateForm"; // 수정 폼 뷰 이름
    }
    @PostMapping("/boards/{id}")
    public String updateBoard(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam String content) {
        boardService.modifyBoard(id, title, content);
        return "redirect:/boards/" + id;
    }
}
