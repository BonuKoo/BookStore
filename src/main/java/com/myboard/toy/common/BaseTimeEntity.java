package com.myboard.toy.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
public abstract class BaseTimeEntity {

    //생성일자
    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    //수정일자
    @LastModifiedDate
    private LocalDate updateDate;
}

/*   @SuperBuilder
     @Builder를 통해 자식 클래스를 생성해주는 과정에서,
     당연히 부모 클래스에 속하는 공통 속성도 함께 생성되어야 합니다.
     해당 어노테이션을 사용하려면, 부모와 자식 클래스에 모두 붙여 사용해주어야 합니다.

    @MappedSuperclass
    BaseEntity를 상속받은 자식 클래스에게,부모 클래스의 매핑 정보를 모두 제공해주는 어노테이션입니다.
    해당 어노테이션은 Entity로 인식되지 않으며, 데이터베이스에 테이블이 생성되지 않습니다.
    이 어노테이션으로 인하여, 상속받은 자식 클래스는 모두 공통 속성을 지니게 됩니다.
 */