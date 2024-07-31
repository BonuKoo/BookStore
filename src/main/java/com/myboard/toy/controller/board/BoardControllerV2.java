package com.myboard.toy.controller.board;

import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.dto.BoardDTO;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import com.myboard.toy.application.board.BoardService;
import com.myboard.toy.application.file.FileService;
import com.myboard.toy.application.file.FileStore;
import com.myboard.toy.domain.reply.dto.ReplyDTO;
import com.myboard.toy.application.reply.ReplyService;
import com.myboard.toy.securityproject.domain.dto.AccountContext;
import com.myboard.toy.securityproject.domain.dto.AccountDto;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.users.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;

@RequestMapping("/boards2")
@Controller
@Slf4j
public class BoardControllerV2 {

    private final BoardService boardService;
    private final ReplyService replyService;
    private final FileService fileService;
    private final FileStore fileStore;
    private final UserService userService;
    public BoardControllerV2(BoardService boardService, ReplyService replyService, FileService fileService, FileStore fileStore, UserService userService) {
        this.boardService = boardService;
        this.replyService = replyService;
        this.fileService = fileService;
        this.fileStore = fileStore;
        this.userService = userService;
    }

    /*
        Version2에는 파일 업로드 로직을 추가한다 !
     */

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
    public String createBoardV2(@ModelAttribute("createBoard") BoardDTO boardDTO,
                                Principal principal,

                                RedirectAttributes redirectAttributes) throws IOException {

        if (principal instanceof UsernamePasswordAuthenticationToken) {

            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
            AccountDto accountDto = (AccountDto) authenticationToken.getPrincipal();
            String username = accountDto.getUsername();

            // username을 토대로 account 값을 db에서 조회한다.
            Account account = userService.getAccountByUsername(username);
            System.out.println("Fetched account: " + account);

            System.out.println("BoardDTO before setting account: " + boardDTO);
            boardDTO.setAccount(account);
            System.out.println("BoardDTO after setting account: " + boardDTO);

            // 게시글 생성
            BoardDTO createdBoard = boardService.createBoardV2(boardDTO);
            redirectAttributes.addAttribute("id", createdBoard.getId());
            return "redirect:/boards2/{id}";
        }
        throw new IllegalArgumentException("Principal is not of type UsernamePasswordAuthenticationToken");
    }


    //댓글 생성
    @PostMapping("/{id}/reply")
    public String createReply(@PathVariable Long id, @RequestParam String content) {
        ReplyDTO replyDTO = ReplyDTO.builder()
                .content(content)
                .boardId(id)
                .build();
        replyService.createReply(replyDTO);
        return "redirect:/boards2/" + id;
    }

    /*
        Read
     */
    //단 건 조회
    @GetMapping("/{id}")
    public String viewDetailBoardWithReplyList(@PathVariable Long id, Model model) {
        BoardDTO boardDTO = boardService.getDetailBoardByIdWithReplyV2(id);

        model.addAttribute("board", boardDTO);
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
        return "redirect:/boards2/" + id;
    }

    /*
        DELETE
     */
    @PostMapping("/{id}/delete")
    public String deleteBoard(@PathVariable Long id) {
        boardService.removeBoard(id);
        return "redirect:/boards2"; // 삭제 후 게시판 목록으로 리다이렉트
    }

}
