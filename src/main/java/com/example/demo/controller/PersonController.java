package com.example.demo.controller;

import com.example.demo.model.Person;
import com.example.demo.services.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class PersonController {

    @Autowired
    PersonService repo;
    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @PostMapping("/addPerson")
    public ResponseEntity<?> addPerson(@RequestBody Person person) {
        try {
            repo.generatePDF(person, person.getName() + " " + person.getLastname() +".pdf");
        } catch (FileNotFoundException e) {
            logger.error("An error occurred during PDF generation: {}", e.getMessage());
        }
        repo.savePerson(person);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/displayPerson")
    public List<Person> getAllPersons(){
        return repo.getAllPersons();
    }

    @DeleteMapping("/deletePerson/{email}")
    public ResponseEntity<String> deletePerson(@PathVariable String email) {
        Person person = repo.findPersonByEmail(email);
        if (person == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person not found");
        repo.deletePersonByEmail(person);
        return ResponseEntity.ok("Person deleted successfully");
    }

    @PutMapping("/updatePerson/{email}")
    public ResponseEntity<String> updatePersonByEmail(@PathVariable String email, @RequestBody Person updatedPerson) {
        Person previousPerson = repo.findPersonByEmail(email);
        if (previousPerson == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person not found");
        repo.updatePerson(previousPerson, updatedPerson);
        return ResponseEntity.ok("Person updated successfully");
    }
}

