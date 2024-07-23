package com.myboard.toy.domain.order.repository;

import com.myboard.toy.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderCustomRepository{

}
