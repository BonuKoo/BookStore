package com.myboard.toy.domain.user;

import com.myboard.toy.domain.address.Address;
import com.myboard.toy.domain.order.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
@Entity
public class User {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;
    
    private String loginId;     //로그인 용 Id
    private String password;    //비밀번호
    private String name;        //이름
    private String role;        //시큐리티 용 권한

    /* 추후 추가 목록 - 세부사항 */

    //private String birthDate;   //생년월일

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

}
