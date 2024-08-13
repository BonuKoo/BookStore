package com.myboard.toy.board.reply.controller;

import com.myboard.toy.board.reply.service.ReplyService;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/replies")
public class ReplyController {

    private final ReplyService replyService;
    private final UserService userService;

    /*
        Create
     */

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createReply(@PathVariable Long id,
                              @RequestParam String content,
                              Principal principal
                              ) {

        AccountDto accountId = userService.getAccountIdByPrincipal(principal);

        replyService.createReply(id, content, accountId);
        return "redirect:/boards/" + id; // 댓글이 생성된 게시글 상세 페이지로 리다이렉트
    }

    /*
        Update
     */

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{boardId}/{replyId}")
    public String updateReply(
            @PathVariable Long boardId,
            @PathVariable Long replyId,
                              @RequestParam("content") String content,
                              Principal principal) {

        replyService.updateReply(replyId, content);
        return "redirect:/boards/" + boardId;
    }

    /*
        Delete
     */

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{boardId}/{replyId}")
    public String deleteReply(@PathVariable Long boardId,
                              @PathVariable Long replyId) {

        replyService.deleteReply(replyId);

        return "redirect:/boards/" + boardId;
    }
}

