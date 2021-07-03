package com.example.demo;

import com.example.demo.controllers.ItemController;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List; 
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItemControllerTest {
    private ItemController itemController;
    private ItemRepository itemRepository = mock(ItemRepository.class);

    public static Item createItem(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setPrice(BigDecimal.valueOf(999.99));
        item.setName("Item #" + id);
        item.setDescription("Item Description #" + id);
        return item;
    }

    @Before
    public void setUp() {
        itemController = new ItemController();
        injectObjects(itemController, "itemRepository", itemRepository);

        Item item1 = createItem(1L);
        Item item2 = createItem(2L);

        when(itemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.findByName(item1.getName())).thenReturn(Arrays.asList(item1));
        when(itemRepository.findByName(item2.getName())).thenReturn(Arrays.asList(item2));
    }

    @Test
    public void getItemsSuccess() {
        final ResponseEntity<List<Item>> response = itemController.getItems();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        List<Item> items = response.getBody();
        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    public void getItemByIdSuccess() {
        final ResponseEntity<Item> response = itemController.getItemById(1L);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        Item item = response.getBody();
        assertNotNull(item);
        assertEquals("Item #1", item.getName());
    }

    @Test
    public void getItemByIdItemNotFound() {
        final ResponseEntity<Item> response = itemController.getItemById(500L);
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getItemsByNameSuccess() {
        final ResponseEntity<List<Item>> response = itemController.getItemsByName("Item #2");
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        List<Item> items = response.getBody();
        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @Test
    public void getItemsByNameItemNotFound() {
        final ResponseEntity<List<Item>> response = itemController.getItemsByName("No Name");
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
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
