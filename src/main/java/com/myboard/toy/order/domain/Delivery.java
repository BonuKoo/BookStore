package com.myboard.toy.order.domain;

import com.myboard.toy.order.domain.status.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Builder;
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

    //@Embedded
    //private Address address;

    private String address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    //private String option; // 예: STANDARD, EXPRESS 등
    /*
    @Builder
    public Delivery(Address address, DeliveryStatus status) {
        this.address = address;
        this.status = status;
    }*/

    @Builder
    public Delivery(String address, DeliveryStatus status) {
        this.address = address;
        this.status = status;
    }
}
