package com.bookService.core.test.cart.mongo.service;

/*
import com.bookService.core.test.cart.mongo.document.CartModelMapDocument1;
import com.bookService.core.test.cart.mongo.repository.CartModelMapRepository1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
 */
/*
@Service
@RequiredArgsConstructor*/
public class CartMongoServiceTest1 {

    /*
    private final MongoTemplate mongoTemplate;
    private final CartModelMapRepository1 cartModelMapRepository1;
    */

    /*
    // 공통: 카트 없으면 upsert로 생성
    private Query userQuery(String userId) {
        return new Query(Criteria.where("userId").is(userId));
    }

    public void addOrIncrease(String userId, String itemIsbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        Query q = userQuery(userId);
        Update u = new Update()
                .inc("items." + itemIsbn + ".quantity", amount)
                .set("updatedAt", Instant.now());
        // 없으면 새 문서 생성
        mongoTemplate.upsert(q, u, CartModelMapDocument1.class);
    }

    public void decrease(String userId, String itemIsbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        // 1) 수량 감소
        Query q = userQuery(userId);
        Update u = new Update()
                .inc("items." + itemIsbn + ".quantity", -amount)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, CartModelMapDocument1.class);

        // 2) 0 이하이면 제거 (단일 쿼리로도 가능하지만 가독성/안전성 우선)
        Query removeQ = new Query(Criteria.where("userId").is(userId)
                .and("items." + itemIsbn + ".quantity").lte(0));
        Update removeU = new Update().unset("items." + itemIsbn);
        mongoTemplate.updateFirst(removeQ, removeU, CartModelMapDocument1.class);
    }

    public void removeItem(String userId, String itemIsbn) {
        Query q = userQuery(userId);
        Update u = new Update().unset("items." + itemIsbn)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, CartModelMapDocument1.class);
    }

    public CartModelMapDocument1 getCart(String userId) {
        CartModelMapDocument1 doc = mongoTemplate.findOne(userQuery(userId), CartModelMapDocument1.class);
        if (doc == null) {
            // 지연 생성 원하면 upsert 대신 여기서 생성해도 OK
            doc = new CartModelMapDocument1(null, userId, new HashMap<>(), Instant.now());
            mongoTemplate.insert(doc);
        }
        return doc;
    }

    // ----------------- Add / Increase -----------------
    public void addOrIncreaseSafe(String userId, String itemIsbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        Query query = new Query(Criteria.where("userId").is(userId));

        // 필드명은 문자열로 정확하게 접근
        Update update = new Update()
                .inc("items." + itemIsbn + ".quantity", amount)
                .set("updatedAt", Instant.now());

        // upsert: 없으면 문서 생성, 있으면 inc 적용
        mongoTemplate.upsert(query, update, CartModelMapDocument1.class);
    }

    // ----------------- Decrease / Remove -----------------
    public void decreaseSafe(String userId, String itemIsbn, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        Query query = new Query(Criteria.where("userId").is(userId));
        Update update = new Update()
                .inc("items." + itemIsbn + ".quantity", -amount)
                .set("updatedAt", Instant.now());

        // atomic 감소
        mongoTemplate.updateFirst(query, update, CartModelMapDocument1.class);

        // 0 이하이면 제거
        Query removeQuery = new Query(Criteria.where("userId").is(userId)
                .and("items." + itemIsbn + ".quantity").lte(0));
        Update removeUpdate = new Update().unset("items." + itemIsbn);
        mongoTemplate.updateFirst(removeQuery, removeUpdate, CartModelMapDocument1.class);
    }

    // ----------------- Remove Item -----------------
    public void removeItemSafe(String userId, String itemIsbn) {
        Query removeQuery = new Query(Criteria.where("userId").is(userId));
        Update removeUpdate = new Update().unset("items." + itemIsbn)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(removeQuery, removeUpdate, CartModelMapDocument1.class);
    }


    @Transactional
    public void clearCart(String userId) {
        CartModelMapDocument1 cart = cartModelMapRepository1.findByUserId(userId)
                .orElseGet(() -> new CartModelMapDocument1(null, userId, new HashMap<>(), Instant.now()));

        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());

        cartModelMapRepository1.save(cart);
    }
    */
}
