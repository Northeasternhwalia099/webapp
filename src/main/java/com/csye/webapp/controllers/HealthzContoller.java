package com.csye.webapp.controllers;

import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.timgroup.statsd.StatsDClient;

@RestController
public class HealthzContoller {
    @Autowired
    StatsDClient statsDClient;
    private final static Logger LOG = LoggerFactory.getLogger(UserController.class);

    @RequestMapping("/healthz")
    public ResponseEntity<?> get() {
        LOG.info("Inside HEALTHZ controller");
        LOG.info("healthz endpoint are hit");
        statsDClient.incrementCounter("healthz api");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}