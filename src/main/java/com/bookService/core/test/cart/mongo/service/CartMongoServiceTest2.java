package com.bookService.core.test.cart.mongo.service;

/*
import com.bookService.core.test.cart.mongo.document.CartItemModelArrayDocument2;
import com.bookService.core.test.cart.mongo.document.CartModelArrayDocument2;
import com.bookService.core.test.cart.mongo.repository.CartModelArrayRepository2;
import com.bookService.core.test.cart.mongo.repository.CartModelMapRepository1;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
*/
//@Service @RequiredArgsConstructor
public class CartMongoServiceTest2 {

    /*
    private final MongoTemplate mongoTemplate;
    private final CartModelArrayRepository2 cartModelArrayRepository2;
    */
    /*
    private Query userQuery(String userId) {
        return new Query(Criteria.where("userId").is(userId));
    }
*/
    // ADD: 있으면 inc, 없으면 push (경합 안전판 포함)
  /*
    public void addItem(String userId, CartItemModelArrayDocument2 item) {
        if (item.getQuantity() <= 0) throw new IllegalArgumentException("amount must be > 0");

        // 1) 기존 항목 inc 시도
        Query q1 = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(item.getItemIsbn()));
        Update u1 = new Update()
                .inc("items.$.quantity", item.getQuantity())
                .set("updatedAt", Instant.now());
        var r1 = mongoTemplate.updateFirst(q1, u1, CartModelArrayDocument2.class);

        if (r1.getMatchedCount() == 0) {
            // 2) 없으면 push 시도 (동시성 중복 방지: $addToSet 사용)
            Query q2 = userQuery(userId);
            Update u2 = new Update()
                    .addToSet("items", new BasicDBObject("itemIsbn", item.getItemIsbn())
                            .append("quantity", item.getQuantity()))
                    .set("updatedAt", Instant.now());
            mongoTemplate.upsert(q2, u2, CartModelArrayDocument2.class);
            // 3) 그래도 경합으로 중복 or 초기화만 된 경우를 대비해 최종 inc 보정
            //    (이미 들어갔으면 +0, 먼저 들어간 다른 쓰기였다면 여기서 +N)
            mongoTemplate.updateFirst(q1, u1, CartModelArrayDocument2.class);
        }
    }

    public void increaseItem(String userId, String isbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        // atomic update: 존재하면 증가
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(isbn));

        Update update = new Update()
                .inc("items.$[elem].quantity", amount)
                .set("updatedAt", Instant.now())
                .filterArray(Criteria.where("elem.itemIsbn").is(isbn));

        UpdateResult result = mongoTemplate.updateFirst(query, update, CartModelArrayDocument2.class);

        //  matchedCount == 0 → 해당 아이템이 없으므로 안전하게 추가
        if (result.getMatchedCount() == 0) {
            Query addQuery = new Query(Criteria.where("userId").is(userId));
            Update addUpdate = new Update()
                    .push("items", new CartItemModelArrayDocument2(isbn, amount))
                    .set("updatedAt", Instant.now());

            mongoTemplate.updateFirst(addQuery, addUpdate, CartModelArrayDocument2.class);
        }
    }

    public void increaseItemSafe(String userId, String isbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(isbn));

        Update update = new Update()
                .inc("items.$[elem].quantity", amount)
                .set("updatedAt", Instant.now())
                .filterArray(Criteria.where("elem.itemIsbn").is(isbn));

        // findAndModify + upsert: 항목이 없으면 새로 추가
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);

        mongoTemplate.findAndModify(
                query,
                update,
                options,
                CartModelArrayDocument2.class
        );

        // 위의 upsert 옵션으로 인해 matchedCount==0 체크 없이 한 번에 처리 가능
    }

    // -------------------- 안전한 증가 --------------------
    public void increaseItemSafe2(String userId, String isbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(isbn));

        Update update = new Update()
                .inc("items.$[elem].quantity", amount)
                .set("updatedAt", Instant.now())
                .filterArray(Criteria.where("elem.itemIsbn").is(isbn));

        // findAndModify + upsert: 없으면 새로 추가
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);

        mongoTemplate.findAndModify(query, update, options, CartModelArrayDocument2.class);
    }



    // DECREASE: 원자적 감소 + 필요 시 제거 (안전 버전)
    public void decreaseItem(String userId, String isbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        Query q = userQuery(userId);
        Update u = new Update()
                .inc("items.$[elem].quantity", -amount)
                .set("updatedAt", Instant.now())
                .filterArray(Criteria.where("elem.itemIsbn").is(isbn));
        mongoTemplate.updateFirst(q, u, CartModelArrayDocument2.class);

        // 감소 후 0 이하만 제거 (해당 ISBN 한정)
        Query rq = userQuery(userId);
        Update ru = new Update().pull("items",
                new BasicDBObject("itemIsbn", isbn)
                        .append("quantity", new BasicDBObject("$lte", 0)));
        mongoTemplate.updateFirst(rq, ru, CartModelArrayDocument2.class);
    }

    // 안전 감소 (동시성 고려)
    public void decreaseItemSafe(String userId, String isbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException();

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(isbn));
        Update update = new Update()
                .inc("items.$[elem].quantity", -amount)
                .set("updatedAt", Instant.now())
                .filterArray(Criteria.where("elem.itemIsbn").is(isbn));

        mongoTemplate.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true),
                CartModelArrayDocument2.class
        );

        // 0 이하 제거
        Query removeQuery = new Query(Criteria.where("userId").is(userId));
        Update removeUpdate = new Update()
                .pull("items", new BasicDBObject("quantity", new BasicDBObject("$lte", 0)));
        mongoTemplate.updateFirst(removeQuery, removeUpdate, CartModelArrayDocument2.class);
    }

    // -------------------- 안전한 감소 --------------------
    public void decreaseItemSafe2(String userId, String isbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        // 1. quantity 감소 (atomic)
        Query q = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(isbn));
        Update u = new Update()
                .inc("items.$[elem].quantity", -amount)
                .set("updatedAt", Instant.now())
                .filterArray(Criteria.where("elem.itemIsbn").is(isbn));

        mongoTemplate.updateFirst(q, u, CartModelArrayDocument2.class);

        // 2. 0 이하 항목 제거 (atomic)
        Query removeQuery = new Query(Criteria.where("userId").is(userId));
        Update removeUpdate = new Update().pull("items",
                new BasicDBObject("itemIsbn", isbn)
                        .append("quantity", new BasicDBObject("$lte", 0)));

        mongoTemplate.updateFirst(removeQuery, removeUpdate, CartModelArrayDocument2.class);
    }



    public void removeItem(String userId, String isbn) {
        Query q = userQuery(userId);
        Update u = new Update()
                .pull("items", new BasicDBObject("itemIsbn", isbn))
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, CartModelArrayDocument2.class);
    }

    public CartModelArrayDocument2 getCart(String userId) {
        return mongoTemplate.findOne(userQuery(userId), CartModelArrayDocument2.class);
    }

    @Transactional
    public void clearCart(String userId) {
        CartModelArrayDocument2 cart = cartModelArrayRepository2.findByUserId(userId)
                .orElseGet(() -> new CartModelArrayDocument2(null, userId, new ArrayList<>(), Instant.now()));

        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());

        cartModelArrayRepository2.save(cart);
    }
    */
}

