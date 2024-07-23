package com.myboard.toy.domain.delivery;

import com.myboard.toy.domain.address.Address;
import com.myboard.toy.domain.order.Order;
import jakarta.persistence.*;
import lombok.Data;

import static jakarta.persistence.FetchType.*;

@Data
@Entity
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery",fetch = LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    public Delivery(Address address, DeliveryStatus status) {
        this.address = address;
        this.status = status;
    }
}
