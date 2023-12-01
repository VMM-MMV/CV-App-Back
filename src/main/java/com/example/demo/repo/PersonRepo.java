package com.example.demo.repo;

import com.example.demo.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface PersonRepo extends JpaRepository<Person, Long> {
    Person findByEmail(String email);
}
