package pl.piomin.services.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import pl.piomin.services.domain.Person;
import pl.piomin.services.repository.PersonRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PersonController {

    private final Logger LOG = LoggerFactory.getLogger(PersonController.class);
    private final PersonRepository repository;

    public PersonController(PersonRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Person> findAll() {
        return (List<Person>) repository.findAll();
    }

    @GetMapping("/{id}")
    public Person findById(@PathVariable("id") Long id) {
        Person obj = repository.findById(id).orElseThrow();
        LOG.info("Found: {}", obj.getId());
        return obj;
    }

    @PostMapping
    public Person add(@RequestBody Person obj) {
        obj.setId(null);
        Person saved = repository.save(obj);
        LOG.info("Added: {}", saved);
        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        repository.deleteById(id);
        LOG.info("Removed: {}", id);
    }

    @PutMapping
    public void update(@RequestBody Person obj) {
        repository.save(obj);
        LOG.info("Updated: {}", obj.getId());
    }

}
