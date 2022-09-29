package com.csye.webapp.controllers;
import java.net.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthzContoller {

    @RequestMapping("/healthz")
    public ResponseEntity<?> get() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}