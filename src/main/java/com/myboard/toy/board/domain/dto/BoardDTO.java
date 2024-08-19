package com.myboard.toy.board.domain.dto;

import com.myboard.toy.infra.file.domain.board.UploadFileOfBoard;
import com.myboard.toy.board.domain.entity.Reply;
import com.myboard.toy.security.domain.entity.Account;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BoardDTO {
    private Long id;
    private String title;
    private String content;
    private Long accountId;
    private Account account;
    //Account -> Account Id로 바꿀 준비
    //성능 시험

    private List<Reply> replies;



    //임시로
    private List<ReplyDTO> replyDTOList;

    private List<MultipartFile> imageFiles; // 파일 업로드 필드
    //service -> 컨트롤러
    private List<UploadFileOfBoard> formattedFiles;

    public BoardDTO(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }


    //Service의 createBoardV2용
    public BoardDTO(Long id, String title, String content,
                    Account account,
                    List<Reply> replies,List<UploadFileOfBoard> formattedFiles) {
        this.id = id;
        this.title = title;
        this.account = account;
        this.content = content;
        this.replies = replies;
        this.formattedFiles = formattedFiles;
    }

    @QueryProjection
    public BoardDTO(Long id, String title, String content, List<Reply> replies) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.replies = replies;
    }

    public void addAccount(Account account) {
        this.account = account;
    }

    public void addAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
