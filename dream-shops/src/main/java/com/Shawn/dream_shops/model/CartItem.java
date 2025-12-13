package com.Shawn.dream_shops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice ;

    @ManyToOne
    @JoinColumn(name="product_id")
    // 因為一個 Product 可以出現在不同的CartItem中
    private Product product;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;

    public void setTotalPrice()
    {
        totalPrice = this.unitPrice.multiply(new BigDecimal(quantity));
    }
}
