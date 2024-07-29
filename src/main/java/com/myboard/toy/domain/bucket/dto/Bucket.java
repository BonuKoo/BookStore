package com.myboard.toy.domain.bucket.dto;

import com.myboard.toy.domain.bucketitem.BucketItem;
import com.myboard.toy.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "buckets")
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bucket_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "bucket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BucketItem> items = new ArrayList<>();

    public void addItem(BucketItem item) {
        items.add(item);
        item.setBucket(this);
    }

    public void removeItem(BucketItem item) {
        items.remove(item);
        item.setBucket(null);
    }
}