package pl.piomin.services;

import org.instancio.Instancio;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.piomin.services.domain.Person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full-context integration tests for the Person REST API. A real Spring Boot
 * application context is started on a random port and exercised end-to-end
 * (controller, JPA repository and in-memory H2 database) through
 * {@link TestRestTemplate}. The methods are ordered so they form a complete
 * lifecycle: create, read, update and finally delete.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonControllerTests {

    private static final String API_PATH = "/api";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void add() {
        Person obj = restTemplate.postForObject(API_PATH, Instancio.create(Person.class), Person.class);
        assertNotNull(obj);
        assertEquals(1, obj.getId());
    }

    @Test
    @Order(2)
    void findAll() {
        Person[] objs = restTemplate.getForObject(API_PATH, Person[].class);
        assertNotNull(objs);
        assertTrue(objs.length > 0);
    }

    @Test
    @Order(2)
    void findById() {
        Person obj = restTemplate.getForObject(API_PATH + "/{id}", Person.class, 1L);
        assertNotNull(obj);
        assertEquals(1, obj.getId());
    }

    @Test
    @Order(3)
    void update() {
        Person obj = restTemplate.getForObject(API_PATH + "/{id}", Person.class, 1L);
        assertNotNull(obj);
        obj.setFirstName("Updated");
        obj.setLastName("Person");

        ResponseEntity<Void> response = restTemplate.exchange(API_PATH, HttpMethod.PUT,
                new HttpEntity<>(obj), Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Person updated = restTemplate.getForObject(API_PATH + "/{id}", Person.class, 1L);
        assertNotNull(updated);
        assertEquals("Updated", updated.getFirstName());
        assertEquals("Person", updated.getLastName());
    }

    @Test
    @Order(4)
    void delete() {
        restTemplate.delete(API_PATH + "/{id}", 1L);
        Person obj = restTemplate.getForObject(API_PATH + "/{id}", Person.class, 1L);
        assertNull(obj.getId());
    }

}
