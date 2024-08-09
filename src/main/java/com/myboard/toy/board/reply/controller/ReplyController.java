package com.myboard.toy.board.reply.controller;

import com.myboard.toy.board.board.service.BoardService;
import com.myboard.toy.board.domain.dto.ReplyDTO;
import com.myboard.toy.board.reply.service.ReplyService;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.users.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/replies")
public class ReplyController {

    private final ReplyService replyService;
    private final BoardService boardService;
    private final UserService userService;

    public ReplyController(ReplyService replyService, BoardService boardService, UserService userService) {
        this.replyService = replyService;
        this.boardService = boardService;
        this.userService = userService;
    }

    /*
        Create
     */

    @PostMapping("/{id}/reply")
    public String createReply(@PathVariable Long id,
                              @RequestParam String content,
                              Principal principal
                              ) {

        AccountDto accountId = userService.getAccountIdByPrincipal(principal);

        ReplyDTO replyDto = ReplyDTO.builder()
                .content(content)
                .boardId(id)
                .accountId(accountId.getId())
                .build();
        replyService.createReply(replyDto);
        return "redirect:/boards2/" + id; // 댓글이 생성된 게시글 상세 페이지로 리다이렉트
    }

    /*
        Update
     */

    @PostMapping("/{replyId}/update")
    public String updateReply(@PathVariable Long replyId, @RequestParam("boardId") Long boardId,
                              @RequestParam("content") String content) {

        ReplyDTO replyDTO = ReplyDTO.builder()
                .id(replyId)
                .content(content)
                .boardId(boardId)
                .build();
        replyService.updateReply(replyId, replyDTO);
        return "redirect:/boards2/" + boardId; // 수정 후 게시글 상세 페이지로 리다이렉트
    }

    //REST

    /*
        Delete
     */

    @PostMapping("/{replyId}/delete")
    public String deleteReply(@PathVariable Long replyId, @RequestParam("boardId") Long boardId) {
        replyService.deleteReply(replyId); // ReplyService를 통해 댓글 삭제 로직 처리
        return "redirect:/boards2/" + boardId; // 삭제 후 게시글 상세 페이지로 리다이렉트
    }

    //REST
    /*
    @DeleteMapping("/{id}")
    public void deleteReply(@PathVariable Long id) {
        replyService.deleteReply(id);
    }
     */
}

