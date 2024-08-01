package com.myboard.toy.controller.board;

import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import com.myboard.toy.application.board.BoardService;
import com.myboard.toy.securityproject.domain.dto.AccountContext;
import com.myboard.toy.securityproject.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
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
    /*
    @GetMapping("/account")
    public AccountDto getAccount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Principal: {}", principal);

        if (principal instanceof AccountContext) {
            AccountContext accountContext = (AccountContext) principal;
            return accountContext.getAccountDto();
        } else if (principal instanceof UserDetails) {
            // principal이 UserDetails의 인스턴스인 경우 처리
            // UserDetails를 AccountContext로 변환하거나 AccountDto를 가져옵니다.
        }
        throw new IllegalStateException("User not authenticated");
    }
    */


}
