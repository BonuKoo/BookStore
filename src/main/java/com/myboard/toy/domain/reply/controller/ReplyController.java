package com.myboard.toy.domain.reply.controller;

import com.myboard.toy.domain.board.service.BoardService;
import com.myboard.toy.domain.reply.dto.ReplyDTO;
import com.myboard.toy.domain.reply.service.ReplyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/replies")
public class ReplyController {

    private final ReplyService replyService;
    private final BoardService boardService;

    public ReplyController(ReplyService replyService,BoardService boardService) {
        this.replyService = replyService;
        this.boardService = boardService;
    }

    /*
        Create
     */

    @GetMapping("/new")
    public String showCreateReplyForm(@RequestParam("boardId") Long boardId, Model model) {
        model.addAttribute("boardId", boardId);
        model.addAttribute("reply", new ReplyDTO());
        return "createReplyForm"; // createReplyForm.html로 이동
    }

    @PostMapping("/new")
    public String createReply(@ModelAttribute("reply") ReplyDTO replyDTO) {
        replyService.createReply(replyDTO); // ReplyService를 통해 댓글 생성 로직 처리
        return "redirect:/boards/" + replyDTO.getBoardId(); // 생성한 댓글이 포함된 게시글 상세 페이지로 리다이렉트
    }


    /*
        Update
     */

    // 수정 페이지로 이동
    @GetMapping("/{replyId}/edit")
    public String showUpdateReplyForm(@PathVariable Long replyId, Model model) {
        ReplyDTO replyDTO = replyService.getReplyById(replyId); // 댓글 ID로 댓글 정보 가져오기
        model.addAttribute("reply", replyDTO);
        return "updateReplyForm"; // updateReplyForm.html로 이동
    }

    @PostMapping("/{replyId}/edit")
    public String updateReply(@PathVariable Long replyId, @ModelAttribute("reply") ReplyDTO replyDTO) {
        replyService.updateReply(replyId, replyDTO); // ReplyService를 통해 댓글 수정 로직 처리
        return "redirect:/boards/" + replyDTO.getBoardId(); // 수정된 댓글이 포함된 게시글 상세 페이지로 리다이렉트
    }

    //REST

    /*
        Delete
     */

    @PostMapping("/{replyId}/delete")
    public String deleteReply(@PathVariable Long replyId, @RequestParam("boardId") Long boardId) {
        replyService.deleteReply(replyId); // ReplyService를 통해 댓글 삭제 로직 처리
        return "redirect:/boards/" + boardId; // 삭제 후 게시글 상세 페이지로 리다이렉트
    }

    //REST
    /*
    @DeleteMapping("/{id}")
    public void deleteReply(@PathVariable Long id) {
        replyService.deleteReply(id);
    }
     */
}

