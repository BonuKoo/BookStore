package com.myboard.toy.domain.board;

import com.myboard.toy.domain.file.UploadFileOfBoard;
import com.myboard.toy.domain.reply.Reply;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Board {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "board",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<UploadFileOfBoard> files = new ArrayList<>();

    @Builder
    public Board(Long id, String title, String content, List<Reply> replies) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.replies = (replies != null) ? replies : new ArrayList<>();
    }

    public Board(String title, String content) {
        this.title = title;
        this.content = content;
        this.replies = new ArrayList<>();
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

    /*
    public void addFile(File file){
        files.add(file);
        file.attachFile(this);
    }

    public void removeFile(File file){
        files.remove(file);
        file.attachFile(null);
    }
     */

}
