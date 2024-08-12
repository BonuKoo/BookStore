package com.myboard.toy.board.board.controller;

import com.myboard.toy.board.domain.dto.BoardSearchCondition;
import com.myboard.toy.board.domain.dto.BoardDTO;
import com.myboard.toy.board.domain.dto.BoardPageDTO;
import com.myboard.toy.board.board.service.BoardService;
import com.myboard.toy.infra.file.service.FileService;
import com.myboard.toy.infra.file.service.FileStore;
import com.myboard.toy.board.reply.service.ReplyService;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;

@RequestMapping("/boards")
@Controller
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final FileService fileService;
    private final FileStore fileStore;
    private final UserService userService;

    /*
        ----------------Create----------------
     */

    @GetMapping("/new")
    public String showCreateBoardForm(Model model) {
        model.addAttribute("createBoard", new BoardDTO());
        return "board/createBoardForm"; // 생성 폼 뷰 이름
    }

    // 게시글 생성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/new")
    public String createBoard(@ModelAttribute("createBoard") BoardDTO boardDTO,
                                Principal principal,
                                RedirectAttributes redirectAttributes) throws IOException {


            Account account = userService.getAccountByPrincipal(principal);

            boardDTO.registerAccount(account);

            // 게시글 생성
            BoardDTO createdBoard = boardService.createBoard(boardDTO);
            redirectAttributes.addAttribute("id", createdBoard.getId());
            return "redirect:/boards/{id}";

    }


    /*
        Read
     */

    @GetMapping("/{id}")
    public String viewDetailBoardWithReplyList(@PathVariable Long id,
                                               Model model,
                                               Principal principal) {

        BoardDTO boardDTO = boardService.getDetailBoardByIdWithReplyV2(id);

        AccountDto loginUser = null;

        if (principal != null) {
          loginUser = userService.getUserDetailsByPrincipal(principal);

        }

        model.addAttribute("board", boardDTO);
        model.addAttribute("principal",loginUser);
        return "board/boardDetail"; // 뷰 이름
    }

    //전체조회
    @GetMapping("")
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

    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @GetMapping("/attach/{id}")
    private ResponseEntity<Resource> downloadAttach(@PathVariable Long id) throws MalformedURLException {
        return fileService.downloadAttach(id);
    }

    /*
        UPDATE
     */

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/update/{id}")
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
        return "redirect:/boards/" + id;
    }

    /*
        DELETE
     */

    // 게시글 삭제 메서드
    @PostMapping("/delete/{id}")
    public String deleteBoard(@PathVariable Long id) {
        boardService.removeBoard(id);
        return "redirect:/boards"; // 삭제 후 전체 게시글 목록으로 리디렉션
    }
}
