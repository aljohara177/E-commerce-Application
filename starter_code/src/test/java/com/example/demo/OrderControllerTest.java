package com.example.demo;

import com.example.demo.controllers.OrderController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays; 
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

    private OrderController orderController;
    private UserRepository userRepository = mock(UserRepository.class);
    private OrderRepository orderRepository = mock(OrderRepository.class);

    public static Item createItem(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setPrice(BigDecimal.valueOf(999.10));
        item.setName("Item #" + id);
        item.setDescription("Item Description #" + id);
        return item;
    }

    public static User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("testPassword");
        return user;
    }

    @Before
    public void setUp() {
        orderController = new OrderController();
        injectObjects(orderController, "userRepository", userRepository);
        injectObjects(orderController, "orderRepository", orderRepository);

        Item item1 = createItem(1L);
        Item item2 = createItem(2L);
        User user = createUser();
        Cart cart = new Cart();
        cart.setUser(user);
        cart.addItem(item1);
        cart.addItem(item2);
        user.setCart(cart);
        UserOrder userOrder = UserOrder.createFromCart(cart);

        when(userRepository.findByUsername("test")).thenReturn(user);
        when(orderRepository.findByUser(any())).thenReturn(Arrays.asList(userOrder));
    }

    @Test
    public void submitSuccess() {
        final ResponseEntity<UserOrder> response = orderController.submit("test");
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        UserOrder userOrder = response.getBody();
        assertNotNull(userOrder);
        assertEquals("test", userOrder.getUser().getUsername());
        assertEquals(2, userOrder.getItems().size());
    }

    @Test
    public void submitUserNotFound() {
        final ResponseEntity<UserOrder> response = orderController.submit("user_not_found");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getOrdersForUserSuccess() {
        final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("test");
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        List<UserOrder> userOrders = response.getBody();
        assertNotNull(userOrders);
        assertEquals(1, userOrders.size());
    }

    @Test
    public void getOrdersForUserUserNotFound() {
        final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("user_not_found");
        assertEquals(404, response.getStatusCodeValue());
    }

    public static void injectObjects(Object target, String fieldName, Object toInject) {

        boolean wasPrivate = false;

        try {
            Field f = target.getClass().getDeclaredField(fieldName);

            if (!f.isAccessible()) {
                f.setAccessible(true);
                wasPrivate = true;
            }
            f.set(target, toInject);
            if (wasPrivate) {
                f.setAccessible(false);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
