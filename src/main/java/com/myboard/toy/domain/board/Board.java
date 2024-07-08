package com.myboard.toy.domain.board;

import com.myboard.toy.domain.reply.Reply;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Getter
@NoArgsConstructor @Builder
@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @Builder
    public Board(Long id, String title, String content, List<Reply> replies) {
        this.id = id;
        this.title = title;
        this.content = content;
        if (replies != null) {
            this.replies = replies;
        }
    }

    public Board(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void addReply(Reply reply){
        replies.add(reply);
        reply.assignBoard(this);
    }

    public void removeReply(Reply reply){
        replies.remove(reply);
        reply.assignBoard(null);
    }

    public void update(String title, String contents){
        if (title != null){
            this.title = title;
        }
        if (contents != null){
            this.content = contents;
        }
    }

}

//오류 상황: UPDATE 하면 title이 contents가 되어버림