package pl.piomin.services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.piomin.services.domain.Person;
import pl.piomin.services.repository.PersonRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link PersonController} using {@link WebMvcTest}. The web layer is
 * loaded in isolation and the {@link PersonRepository} is mocked so each REST endpoint
 * can be exercised independently of the persistence layer.
 */
@WebMvcTest(PersonController.class)
class PersonControllerWebMvcTest {

    private static final String API_PATH = "/api";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PersonRepository repository;

    private Person newPerson(Long id, String firstName, String lastName) {
        Person person = new Person();
        person.setId(id);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmail(firstName.toLowerCase() + "@example.com");
        person.setPhoneNumber("123456789");
        person.setDateOfBirth(LocalDate.of(1990, 1, 1));
        person.setGender("M");
        person.setAddress("Test Street 1");
        return person;
    }

    @Test
    void shouldReturnAllPersons() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
                newPerson(1L, "John", "Doe"),
                newPerson(2L, "Jane", "Doe")));

        mockMvc.perform(get(API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(repository, times(1)).findAll();
    }

    @Test
    void shouldReturnPersonById() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(newPerson(1L, "John", "Doe")));

        mockMvc.perform(get(API_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(repository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnServerErrorWhenPersonNotFound() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_PATH + "/{id}", 999L))
                .andExpect(status().isInternalServerError());

        verify(repository, times(1)).findById(999L);
    }

    @Test
    void shouldAddPerson() throws Exception {
        Person request = newPerson(null, "John", "Doe");
        Person saved = newPerson(1L, "John", "Doe");
        when(repository.save(any(Person.class))).thenReturn(saved);

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(repository, times(1)).save(any(Person.class));
    }

    @Test
    void shouldUpdatePerson() throws Exception {
        Person request = newPerson(1L, "John", "Updated");
        when(repository.save(any(Person.class))).thenReturn(request);

        mockMvc.perform(put(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(repository, times(1)).save(any(Person.class));
    }

    @Test
    void shouldDeletePerson() throws Exception {
        mockMvc.perform(delete(API_PATH + "/{id}", 1L))
                .andExpect(status().isOk());

        verify(repository, times(1)).deleteById(eq(1L));
        verify(repository, never()).save(any(Person.class));
    }
}
