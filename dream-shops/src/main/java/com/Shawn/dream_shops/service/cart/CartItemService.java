package com.Shawn.dream_shops.service.cart;

import com.Shawn.dream_shops.exceptions.ResourceNotFoundException;
import com.Shawn.dream_shops.model.Cart;
import com.Shawn.dream_shops.model.CartItem;
import com.Shawn.dream_shops.model.Product;
import com.Shawn.dream_shops.repository.CartItemRepository;
import com.Shawn.dream_shops.repository.CartRepository;
import com.Shawn.dream_shops.service.product.IProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService{

    @Autowired
    private final CartItemRepository CartItemRepo;
    @Autowired
    private final CartRepository CartRepo;
    @Autowired
    private final IProductService ProductService;
    @Autowired
    private final ICartService CartService;

    @Transactional
    @Override
    public void addCartItem(Long cartID, Long productId, int quantity) {
        Cart cart = CartService.getCart(cartID);
        Product product = ProductService.getProductById(productId);
        CartItem cartItem = cart.getItems()
                                .stream()
                                .filter(item -> item.getProduct().getId().equals(productId))
                                .findFirst()
                                .orElse(new CartItem());

        if(cartItem.getId() == null) // 代表沒有相同的 Product 在 Cart 中了
        {
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
        }
        else // 代表已經有相同的 Product 在 Cart 中了
        {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            // 加上這次要買的數量
        }

        // 最後要儲存到資料庫
        cartItem.setTotalPrice();
        cart.addItem(cartItem);
        CartRepo.save(cart);
    }

    @Override
    public void removeItemFromCart(Long cartId, Long productId) {
        Cart cart = CartService.getCart(cartId);
        CartItem itemToRemove = getCartItem(cartId, productId);
        // 在下方的函式

        cart.removeItem(itemToRemove);
        CartRepo.save(cart);
    }

    @Override
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        Cart cart = CartService.getCart(cartId);

        cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(
                        item -> {
                            item.setQuantity(quantity);
//                            item.setUnitPrice(item.getProduct().getPrice()); // 其實不應該改道 UnitPrice
                            item.setTotalPrice();
                        });

        // 因為 單一數量會改變 所以 整體數量也會改變
//        BigDecimal totalAmount = cart.getTotalAmount();
//        這樣寫會取回之前沒有更新的 TotalAmount 的值
        BigDecimal totalAmount = cart.getItems().stream()
                .map(CartItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(totalAmount);

        // 更新資料庫的值
        CartRepo.save(cart);
    }

    @Override
    public CartItem getCartItem(Long cartId, Long productId)
    {
        Cart cart = CartService.getCart(cartId);
        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item Not Found"));
    }
}
