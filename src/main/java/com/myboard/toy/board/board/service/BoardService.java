package com.myboard.toy.board.board.service;

import com.myboard.toy.board.board.repository.BoardRepository;
import com.myboard.toy.board.domain.Board;
import com.myboard.toy.board.domain.dto.BoardSearchCondition;
import com.myboard.toy.board.domain.dto.BoardDTO;
import com.myboard.toy.board.domain.dto.BoardPageDTO;
import com.myboard.toy.board.domain.dto.ReplyDTO;
import com.myboard.toy.infra.file.domain.board.UploadFileOfBoard;
import com.myboard.toy.infra.file.service.FileStore;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BoardService {

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
    //TODO 수정 필요 - QueryDsl Class도 참고해야함
    public BoardDTO getDetailBoardByIdWithReplyV2(Long id){

        Board board = boardRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " +id));
        
        List<ReplyDTO> replyDTOS = board.getReplies().stream()
                .map(reply -> ReplyDTO.builder()
                        .id(reply.getId())
                        .content(reply.getContent())
                        .boardId(reply.getBoard().getId())
                        .accountId(reply.getAccount().getId())
                        .build()
                )
                .toList();

        return BoardDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
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

    //생성
    public BoardDTO createBoard(BoardDTO boardDTO){

        Board board = new Board(boardDTO.getTitle(), boardDTO.getContent());
        Board savedBoard = boardRepository.save(board);
        return new BoardDTO(savedBoard.getId(), savedBoard.getTitle(), savedBoard.getContent());
    }

    //파일 업로드 기능 추가
    public BoardDTO createBoardV2(BoardDTO boardDTO) throws IOException {

        Board board = Board.builder()
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .account(boardDTO.getAccount())
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
