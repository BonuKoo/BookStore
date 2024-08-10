package com.myboard.toy.security.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "ROLE")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "role_id")
    private Long id;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "role_desc")
    private String roleDesc;

    @Column(name = "is_expression")
    private String isExpression;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roleSet", cascade = CascadeType.ALL)
    @OrderBy("orderNum desc")
    private Set<Resources> resourcesSet = new LinkedHashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "userRoles", cascade = CascadeType.ALL)
    private Set<Account> accounts = new HashSet<>();

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", roleName='" + roleName + '\'' +
                ", roleDesc='" + roleDesc + '\'' +
                ", isExpression='" + isExpression + '\'' +
                '}';
    }

}

