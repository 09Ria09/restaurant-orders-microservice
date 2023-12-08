package nl.tudelft.sem.template.example.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class OrderController {

    /**
     * Creates a new Order Controller.
     */
    @Autowired
    public OrderController() {
    }

    @GetMapping("/hi/")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello more");

    }

}
