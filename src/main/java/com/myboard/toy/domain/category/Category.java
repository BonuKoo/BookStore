package com.myboard.toy.domain.category;

import com.myboard.toy.domain.item.Item;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//@Data
//@Entity
public class Category {
    /*
    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;
    
    private String name;
    

    @ManyToMany
    @JoinTable(name = "category_item",
    joinColumns = @JoinColumn(name = "category_id"),
    inverseJoinColumns = @JoinColumn(name = "isbn"))
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();
    */
    /* 연관관계 메서드 */
    /*
    public void addChildCategory(Category child){
        this.child.add(child);
        child.setParent(this);
    }*/
}
