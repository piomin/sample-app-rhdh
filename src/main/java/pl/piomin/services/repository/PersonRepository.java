package pl.piomin.services.repository;

import org.springframework.data.repository.CrudRepository;
import pl.piomin.services.domain.Person;

public interface PersonRepository extends CrudRepository<Person, Long> {
}
