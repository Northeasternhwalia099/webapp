package com.csye.webapp.controllers;

import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timgroup.statsd.StatsDClient;

@RestController
public class HealthzContoller {
    @Autowired
    StatsDClient statsDClient;

    @RequestMapping("/healthz")
    public ResponseEntity<?> get() {
        statsDClient.incrementCounter("create user api");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}