package com.myboard.toy.order.order.repository;

import com.myboard.toy.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderCustomRepository{

}
