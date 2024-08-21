package com.myboard.toy.board.domain.entity;

import com.myboard.toy.infra.file.domain.board.UploadFileOfBoard;
import com.myboard.toy.security.domain.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    //계정 추가
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    //@Builder.Default
    @OneToMany(mappedBy = "board",cascade = CascadeType.ALL,orphanRemoval = true,
    fetch = FetchType.EAGER)
    private List<UploadFileOfBoard> files = new ArrayList<>();

    //조회 수
    /*
    private int cnt;
     */

    @Builder
    public Board(Long id, String title,String content,Account account, List<Reply> replies, List<UploadFileOfBoard> files) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.account = account;
        this.replies = (replies != null) ? replies : new ArrayList<>();
        this.files = (files != null) ? files : new ArrayList<>();
    }

    public Board(String title, String content,List<UploadFileOfBoard> files) {
        this.title = title;
        this.content = content;
        this.replies = new ArrayList<>();
        this.files = (files != null) ? files : new ArrayList<>();
    }

    public Board(String title, String content) {
        this.title = title;
        this.content = content;
        this.replies = (replies != null) ? replies : new ArrayList<>();
        this.files = (files != null) ? files : new ArrayList<>();
    }

    public void update(String title, String contents){
        if (title != null){
            this.title = title;
        }
        if (contents != null){
            this.content = contents;
        }
    }

    /*
        Reply 연관
     */

    public void addReply(Reply reply){
        replies.add(reply);
        reply.assignBoard(this);
    }

    public void removeReply(Reply reply){
        replies.remove(reply);
        reply.assignBoard(null);
    }

    /*
        File 연관
     */

    public void addFile(UploadFileOfBoard file) {
        files.add(file);
        file.attachBoard(this);
    }

    public void removeFile(UploadFileOfBoard file) {
        files.remove(file);
        file.attachBoard(null);
    }

}
