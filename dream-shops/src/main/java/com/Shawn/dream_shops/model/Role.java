package com.Shawn.dream_shops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.sql.ast.tree.expression.Collation;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    // ROLE_USER ROLE_ADMIN ROLE_MANAGER

    public Role(String name) {
        this.name = name;
    }

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Collection<User> users = new HashSet<>();
    // List, Set, Queue … 都是繼承 Collection 的子介面。 所以可以打 Collection 再賦予它不同的 Data Type
}
