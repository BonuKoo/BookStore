package com.myboard.toy.order.domain;

import com.myboard.toy.order.domain.status.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Data;

import static jakarta.persistence.FetchType.*;

@Data
@Entity
public class Delivery {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
