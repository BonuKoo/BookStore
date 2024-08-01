package com.myboard.toy.controller.board;

import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import com.myboard.toy.application.board.BoardService;

import com.myboard.toy.domain.reply.dto.ReplyDTO;
import com.myboard.toy.application.reply.ReplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/boards1")
@Controller
@Slf4j
public class BoardController {
    private final BoardService boardService;
    private final ReplyService replyService;

    public BoardController(BoardService boardService,ReplyService replyService) {
        this.boardService = boardService;
        this.replyService = replyService;
    }

    /*
        Create
     */

    @GetMapping("/new")
    public String showCreateBoardForm(Model model) {
        model.addAttribute("createBoard", new BoardDTO());
        return "board/createBoardForm"; // 생성 폼 뷰 이름
    }

    @PostMapping("/new")
    public String createBoard(@ModelAttribute("createBoard") BoardDTO boardDTO) {
        BoardDTO createdBoard = boardService.createBoard(boardDTO);
        return "redirect:/boards1/" + createdBoard.getId();
        // 생성 후 상세 페이지로 리다이렉트
    }


    @PostMapping("/{id}/reply")
    public String createReply(@PathVariable Long id, @RequestParam String content) {
        ReplyDTO replyDTO = ReplyDTO.builder()
                .content(content)
                .boardId(id)
                .build();
        replyService.createReply(replyDTO);
        return "redirect:/boards1/" + id;
    }

    /*
        Read
     */

    @GetMapping("/{id}")
    public String viewDetailBoardWithReplyList(@PathVariable Long id, Model model) {
        BoardDTO boardDTO = boardService.getDetailBoardByIdWithReply(id);

        model.addAttribute("board", boardDTO);
        return "board/boardDetail"; // 뷰 이름
    }

//    @GetMapping("")
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

        return "board/boardList";
    }

    /*
        UPDATE
     */

    @GetMapping("/{id}/edit")
    public String modifyBoard(@PathVariable Long id, Model model) {
        BoardDTO boardDTO = boardService.getDetailBoardByIdWithReply(id);
        model.addAttribute("board", boardDTO);
        return "board/updateForm"; // 수정 폼 뷰 이름
    }

    @PostMapping("/{id}")
    public String modifyBoard(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam String content) {
        boardService.modifyBoard(id, title, content);
        return "redirect:/boards1/" + id;
    }

    /*
        DELETE
     */
    @PostMapping("/{id}/delete")
    public String deleteBoard(@PathVariable Long id) {
        boardService.removeBoard(id);
        return "redirect:/boards1"; // 삭제 후 게시판 목록으로 리다이렉트
    }

}
