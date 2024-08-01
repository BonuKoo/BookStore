package com.myboard.toy.infrastructure.order;


import com.myboard.toy.domain.order.Order;
import com.myboard.toy.domain.order.researchcondition.OrderSearch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.util.StringUtils;

import java.util.List;

public class OrderCustomRepositoryImpl implements OrderCustomRepository{

    private EntityManager em;

    public OrderCustomRepositoryImpl(EntityManager em) {
        this.em = em;
    }


    //TODO : QueryDSL로 한번에 처리하도록 보수
    public List<Order> findAll(OrderSearch orderSearch){
        String jpql = "select o From Order o join o.user u";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getUserName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " u.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getUserName())) {
            query = query.setParameter("name", orderSearch.getUserName());
        }
        return query.getResultList();
    }
}
