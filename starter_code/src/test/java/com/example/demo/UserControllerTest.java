package com.example.demo;

import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private UserController userController;

    private UserRepository userRepository = mock(UserRepository.class);

    private CartRepository cartRepository = mock(CartRepository.class);

    private BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);

    @Before
    public void setup() {
        userController = new UserController();
        UserControllerTest.injectObjects(userController, "userRepository", userRepository);
        UserControllerTest.injectObjects(userController, "cartRepository", cartRepository);
        UserControllerTest.injectObjects(userController, "bcryptEncoder", bCryptPasswordEncoder);
    }

    @Test
    public void createUser() throws Exception {
        when(bCryptPasswordEncoder.encode("testpassword")).thenReturn("hashedPassword");

        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("test");
        userRequest.setPassword("testpassword");
        userRequest.setConfirmPassword("testpassword");

        ResponseEntity<User> response = userController.createUser(userRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User user = response.getBody();
        assertNotNull(user);
        assertEquals(0, user.getId());
        assertEquals("test", user.getUsername());
        assertEquals("hashedPassword", user.getPassword());
    }

    @Test
    public void createUserWithFailure() {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("test");
        userRequest.setPassword("password");
        userRequest.setConfirmPassword("passwordd");

        ResponseEntity<User> response = userController.createUser(userRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void findUser() {
        User user = new User();
        user.setId((long)1);
        user.setUsername("test");
        user.setPassword("password");

        when(userRepository.findByUsername("test")).thenReturn(user);

        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("test");
        userRequest.setPassword("password");
        userRequest.setConfirmPassword("password");

        userController.createUser(userRequest);

        ResponseEntity<User> response = userController.findByUserName("test");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test", user.getUsername());
    }

    @Test
    public void findUserFailedTest() {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("test");
        userRequest.setPassword("testpassword");
        userRequest.setConfirmPassword("testpassword");

        ResponseEntity<User> findUser = userController.findByUserName("test_user");

        assertNotNull(userController.findByUserName("test_user"));
        assertEquals(HttpStatus.NOT_FOUND, findUser.getStatusCode());
    }

    @Test
    public void findUserById() {
        User user = new User();
        user.setId(1);
        user.setUsername("test");
        user.setPassword("testpassword");
        when(userRepository.findById((long)1)).thenReturn(java.util.Optional.of(user));

        ResponseEntity<User> response = userController.findById((long)1);

        assertNotNull(response);
        assertEquals("test", response.getBody().getUsername());
    }


    public static void injectObjects(Object target, String fieldName, Object toInject){

        boolean wasPrivate = false;
        try {
            Field f = target.getClass().getDeclaredField(fieldName);

            if(!f.isAccessible()){
                f.setAccessible(true);
                wasPrivate = true;
            }
            f.set(target, toInject);
            if(wasPrivate){
                f.setAccessible(false);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }


    }

}
