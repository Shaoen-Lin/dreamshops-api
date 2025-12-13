package com.Shawn.dream_shops.service.order;

import com.Shawn.dream_shops.dto.OrderDto;
import com.Shawn.dream_shops.enums.OrderStatus;
import com.Shawn.dream_shops.exceptions.ResourceNotFoundException;
import com.Shawn.dream_shops.model.Cart;
import com.Shawn.dream_shops.model.Order;
import com.Shawn.dream_shops.model.OrderItem;
import com.Shawn.dream_shops.model.Product;
import com.Shawn.dream_shops.repository.OrderRepository;
import com.Shawn.dream_shops.repository.ProductRepository;
import com.Shawn.dream_shops.service.cart.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{

    @Autowired
    private final OrderRepository orderRepo;
    @Autowired
    private final ProductRepository productRepo;
    @Autowired
    private final CartService cartService;
    @Autowired
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Order placeOrder(Long userId) { // 每次購買都會把購物車清空
        Cart cart = cartService.getCartByUserId(userId);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found for user id: " + userId);
        }

        // 會先設置好一些訂單內容
        Order order = createOrder(cart);
        // 把購物車的設定為 訂單項目
        List<OrderItem> orderItemList = createOrderItems(order, cart);

        // 設定此次的訂單
        order.setOrderItems(new HashSet<>(orderItemList));
        order.setTotalAmount(calculateTotalAmount(orderItemList));

        // 成立訂單
        Order savedOrder = orderRepo.save(order);

        // 每次購買完就會把購物車清空
        cartService.clearCart(cart.getId());

        return savedOrder;
    }

    private Order createOrder(Cart cart)
    {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart)
    {
        // 每次購買都會把購物車清空
        return cart.getItems()
                .stream() // 變成 Stream<CartItem>
                .map(cartItem ->
                {   // cartItem 其實就是 product
                    Product product = cartItem.getProduct();
                    product.setInventory(product.getInventory() - cartItem.getQuantity());
                    // 把 庫存 - 訂購的數量 扣掉
                    productRepo.save(product);

                    // map 結束都必須return 一個物件回傳給一個物件 因為map就是一對一轉換的意思
                    return new OrderItem(
                            order,
                            product,
                            cartItem.getQuantity(),
                            cartItem.getUnitPrice()
                    );
                }) // Stream<CartItem> 轉換成 Stream<OrderItem>。
                .toList(); // toList 就是在把 Stream 轉成 List = 轉換成 List<OrderItem>
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList)
    {
        return  orderItemList.stream()
                .map(item -> item.getPrice()
                                           .multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);

    }

    @Override
    public OrderDto getOrder(Long orderId) {
        return orderRepo.findById(orderId).map(this::convertToDto)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId)
    {
        List<Order> orders = orderRepo.findByUserId(userId);
        return orders.stream().map(this::convertToDto).toList();
    }

    @Override
    public OrderDto convertToDto(Order order)
    {
        return modelMapper.map(order, OrderDto.class);
    }
}
