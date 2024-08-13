package com.myboard.toy.board.board.service;

import com.myboard.toy.board.board.repository.BoardRepository;
import com.myboard.toy.board.domain.Board;
import com.myboard.toy.board.domain.dto.BoardSearchCondition;
import com.myboard.toy.board.domain.dto.BoardDTO;
import com.myboard.toy.board.domain.dto.BoardPageDTO;
import com.myboard.toy.board.domain.dto.ReplyDTO;
import com.myboard.toy.common.exception.UserNotFoundException;
import com.myboard.toy.infra.file.domain.board.UploadFileOfBoard;
import com.myboard.toy.infra.file.service.FileStore;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.repository.UserRepository;
import com.myboard.toy.security.users.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BoardService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final FileStore fileStore;

    //리스트
    public Page<BoardPageDTO> searchWithPage(BoardSearchCondition condition, Pageable pageable){
        return boardRepository.searchWithPage(condition, pageable);
    }

    //단 건 조회
    public BoardDTO getDetailBoardByIdWithReply(Long id){
        Board board = boardRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " +id));

        return BoardDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .replies(board.getReplies())
                .build();
    }

    //단 건 조회 + 파일도 불러오기 실험

    public BoardDTO getDetailBoardByIdWithReplyV2(Long id){

        Board board = boardRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " +id));
        
        List<ReplyDTO> replyDTOS = board.getReplies().stream()
                .map(reply -> ReplyDTO.builder()
                        .id(reply.getId())
                        .content(reply.getContent())
                        .boardId(reply.getBoard().getId())
                        .account(reply.getAccount())
                        .build()
                )
                .toList();

        return BoardDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .accountId(board.getAccount().getId())
                .replyDTOList(replyDTOS)
                .formattedFiles(board.getFiles())
                .build();
    }


    //수정
    public BoardDTO modifyBoard(Long id, String title, String content){

        Board board = boardRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("해당하는 게시글을 찾을 수 없습니다. ID:"+id));

        board.update(title,content);
        Board modifiedBoard = boardRepository.save(board);

        return new BoardDTO(modifiedBoard.getId(), modifiedBoard.getTitle(), modifiedBoard.getContent());
    }

    //삭제
    public void removeBoard(Long id){
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 게시글을 찾을 수 없습니다. ID:" + id));

        boardRepository.delete(board);
    }


    //파일 업로드 기능 추가
    public BoardDTO createBoard(BoardDTO boardDTO) throws IOException {
        Long accountId = boardDTO.getAccountId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();
        Board board = Board.builder()
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .account(account)
                .build();

        // 파일 저장 로직
        List<UploadFileOfBoard> savedFiles = boardDTO.getImageFiles().stream()
                .map(file -> {
                    try {

                        UploadFileOfBoard uploadedFile = fileStore.storeFile(file);

                        //업로드 파일이 null이 아닌 경우에 추가하도록 변경
                        if (uploadedFile != null) {
                            board.addFile(uploadedFile);
                        }
                        return uploadedFile;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        Board savedBoard = boardRepository.save(board);

        return new BoardDTO(savedBoard.getId(),
                savedBoard.getTitle(),
                savedBoard.getContent(),
                savedBoard.getAccount(),
                savedBoard.getReplies(),
                savedBoard.getFiles());
    }

}
