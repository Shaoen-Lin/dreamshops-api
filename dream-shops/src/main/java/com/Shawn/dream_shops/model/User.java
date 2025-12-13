package com.Shawn.dream_shops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @NaturalId // 除了主鍵 (@Id) 之外，可以唯一識別一筆資料的欄位。
    private String email;
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orderList;

    //多對多 關聯裡，會需要一張 中間表
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    // fk 就是 User 的 id 跟 role 的 id
    // id 指的就是 User 或 Role 實體上標記 @Id 的欄位名稱
    private Collection<Role> roles = new HashSet<>();
    //cascade 告訴 Hibernate 當前實體在做某些動作時，是否要連帶影響關聯的實體。
    //為什麼不用 Cascade.ALL？ 因為會包含 CascadeType.REMOVE
    // 為什麼沒加 CascadeType.REMOVE？ 因為你不會希望刪掉使用者時，連 role 也刪掉。
}
