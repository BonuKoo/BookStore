package com.myboard.toy.domain.board;

import com.myboard.toy.domain.reply.Reply;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String contents;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    public void addReply(Reply reply){
        replies.add(reply);
        reply.assignBoard(this);
    }

    public void removeReply(Reply reply){
        replies.remove(reply);
        reply.assignBoard(null);
    }

}
