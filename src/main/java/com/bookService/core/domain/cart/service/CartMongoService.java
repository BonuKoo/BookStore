package com.bookService.core.domain.cart.service;

import com.bookService.core.domain.cart.dto.CartDocument;
import com.bookService.core.domain.cart.repository.CartMongoRepository;
import com.bookService.core.domain.cartitem.dto.CartItemDocument;
//import com.mongodb.BasicDBObject;
import lombok.RequiredArgsConstructor;

/*
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
*/

import org.springframework.stereotype.Service;

import java.util.Optional;

//@Service
//@RequiredArgsConstructor
public class CartMongoService {

    /*
    private final CartMongoRepository cartMongoRepository;
    private final MongoTemplate mongoTemplate;
    // Cart 조회
    public CartDocument getCart(String userId) {
        return cartMongoRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CartDocument newCart = new CartDocument();
                    newCart.setUserId(userId);
                    return cartMongoRepository.save(newCart);
                });
    }
    // 장바구니 아이템 추가 (동일 ISBN 있으면 수량 증가, 없으면 push)
    public void addItem(String userId, CartItemDocument item) {
        // 먼저 해당 아이템 존재 여부 체크
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(item.getItemIsbn()));

        Update update = new Update().inc("items.$.quantity", item.getQuantity());
        var result = mongoTemplate.updateFirst(query, update, CartDocument.class);

        if (result.getMatchedCount() == 0) {
            // 해당 아이템이 없으면 push
            Query cartQuery = new Query(Criteria.where("userId").is(userId));
            Update pushUpdate = new Update().push("items", item);
            mongoTemplate.updateFirst(cartQuery, pushUpdate, CartDocument.class);
        }
    }

    // 수량 증가 (원자적)
    public void increaseItem(String userId, String itemIsbn, int amount) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(itemIsbn));

        Update update = new Update().inc("items.$.quantity", amount);

        // 기존 아이템 있으면 증가
        var result = mongoTemplate.updateFirst(query, update, CartDocument.class);

        // 없으면 새 아이템 추가
        if (result.getMatchedCount() == 0) {
            CartItemDocument newItem = new CartItemDocument(itemIsbn, amount);
            Query cartQuery = new Query(Criteria.where("userId").is(userId));
            Update cartUpdate = new Update().push("items", newItem);
            mongoTemplate.updateFirst(cartQuery, cartUpdate, CartDocument.class);
        }
    }

    // 수량 감소 (원자적, 0 이하일 경우 자동 제거)
    public void decreaseItem(String userId, String itemIsbn, int amount) {
        // 수량 감소
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(itemIsbn));
        Update update = new Update().inc("items.$.quantity", -amount);
        mongoTemplate.updateFirst(query, update, CartDocument.class);

        // 수량이 0 이하인 아이템 제거
        Query removeQuery = new Query(Criteria.where("userId").is(userId));
        Update removeUpdate = new Update().pull("items",
                new Query(Criteria.where("itemIsbn").is(itemIsbn)
                        .and("quantity").lte(0)).getQueryObject());
        mongoTemplate.updateFirst(removeQuery, removeUpdate, CartDocument.class);
    }

    // 병목 줄이기
    public void decreaseItem1(String userId, String itemIsbn, int amount) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("items.itemIsbn").is(itemIsbn));

        Update update = new Update()
                .inc("items.$.quantity", -amount)  // 수량 감소
                .pull("items", new BasicDBObject("quantity", new BasicDBObject("$lte", 0)));
        // 수량이 0 이하인 아이템은 제거

        mongoTemplate.updateFirst(query, update, CartDocument.class);
    }

    // 원자적으로 수량 감소 + 필요 시 제거
    public void decreaseItem2(String userId, String itemIsbn, int amount) {
        Query query = new Query(Criteria.where("userId").is(userId));

        Update update = new Update()
                // 해당 ISBN 아이템만 수량 감소
                .inc("items.$[elem].quantity", -amount)
                // 조건을 만족하는 아이템 제거 (itemIsbn + quantity ≤ 0)
                .pull("items", new BasicDBObject("itemIsbn", itemIsbn)
                        .append("quantity", new BasicDBObject("$lte", 0)));

        // arrayFilters 조건 설정
        update.filterArray(Criteria.where("elem.itemIsbn").is(itemIsbn));

        // 실행
        mongoTemplate.updateFirst(query, update, CartDocument.class);
    }


    // 장바구니에서 아이템 제거
    public void removeItem(String userId, String itemIsbn) {
        Query query = new Query(Criteria.where("userId").is(userId));
        Update update = new Update().pull("items", new Query(Criteria.where("itemIsbn").is(itemIsbn)).getQueryObject());
        mongoTemplate.updateFirst(query, update, CartDocument.class);
    }
*/
}
