package com.Shawn.dream_shops.service.cart;

import com.Shawn.dream_shops.exceptions.ResourceNotFoundException;
import com.Shawn.dream_shops.model.Cart;
import com.Shawn.dream_shops.model.User;
import com.Shawn.dream_shops.repository.CartItemRepository;
import com.Shawn.dream_shops.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService{

    @Autowired
    private CartRepository CartRepo;
    @Autowired
    private CartItemRepository CartItemRepo;

    private final AtomicLong cartIdGenerator = new AtomicLong(0);
    // 原子性物件 避免 Race Condition
    // 提供朵個函數來操作他

    @Override
    public Cart getCart(Long id) {
        Cart cart = CartRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cart Not Found!"));
        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);
        return CartRepo.save(cart);

        // 每次呼叫 getCart 時，都要重新 SetTotalAmount;
    }

    @Transactional
    // remove 這種會修改資料庫狀態的動作，必須放在 transaction 中才能正確執行
    @Override
    public void clearCart(Long id) {
        Cart cart = getCart(id);
        //CartItemRepo.deleteAllByCartId(id); // 這個 Cart 的 所有 item 都要刪除 才是 deleteAll
        cart.getItems().clear(); // getItems 是因為 Lombok
        cart.setTotalAmount(BigDecimal.ZERO);
        CartRepo.save(cart);       // <--- 改成這樣
    }

    @Override
    public BigDecimal getTotalPrice(Long id) { // 計算某個購物車的「總金額」
        Cart cart = getCart(id);
//        System.out.println(cart.updateTotalAmount() + ", Cart id is " + id);
        return cart.updateTotalAmount();
    }

    @Override
    public Cart initialNewCart(User user){
        return Optional.ofNullable(getCartByUserId(user.getId())) // 有 Cart 存在
                .orElseGet( () -> {         // 沒有 Cart 存在
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return CartRepo.save(cart);
                });
    }

    @Override
    public Cart getCartByUserId(Long userId) {
        return CartRepo.findByUserId(userId);
    }
}
