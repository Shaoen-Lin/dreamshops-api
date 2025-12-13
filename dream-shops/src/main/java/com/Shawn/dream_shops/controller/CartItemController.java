package com.Shawn.dream_shops.controller;

import com.Shawn.dream_shops.exceptions.ResourceNotFoundException;
import com.Shawn.dream_shops.model.Cart;
import com.Shawn.dream_shops.model.User;
import com.Shawn.dream_shops.reponse.ApiResponse;
import com.Shawn.dream_shops.service.cart.ICartItemService;
import com.Shawn.dream_shops.service.cart.ICartService;
import com.Shawn.dream_shops.service.user.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cartItems")
public class CartItemController {
    @Autowired
    private final ICartItemService cartItemService;
    @Autowired
    private final ICartService cartService;
    @Autowired
    private final UserService userService;

    @PostMapping("/item/add")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestParam Long productId,
                                                     @RequestParam Integer quantity)
    {
        try {
            User user = userService.getAuthenticatedUser();

            if (user == null) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse("User not found in database. Please login again.", null));
            }

            // 邏輯：先檢查有沒有車，沒有才建
            Cart cart = cartService.getCartByUserId(user.getId());
            if (cart == null) {
                cart = cartService.initialNewCart(user);
            }
            cartItemService.addCartItem(cart.getId(), productId, quantity);
            return ResponseEntity.ok(new ApiResponse("Successfully Add Item to Cart id: " + cart.getId(), cart.getId()));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        } catch (JwtException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(),null));
        }
    }

    @DeleteMapping("/cart/{cartId}/item/{productId}/remove")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable Long cartId, @PathVariable Long productId)
    {
        try {
            cartItemService.removeItemFromCart(cartId, productId);
            return ResponseEntity.ok(new ApiResponse("Remove Item Success",null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        }

    }

    @PutMapping("/cart/{cartId}/item/{productId}/update")
    public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable Long cartId,
                                                          @PathVariable Long productId,
                                                          @RequestParam Integer quantity)
    {
        try {
            cartItemService.updateItemQuantity(cartId, productId, quantity);
            return ResponseEntity.ok(new ApiResponse("Update Item Quantity Success",null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        }

    }
}
