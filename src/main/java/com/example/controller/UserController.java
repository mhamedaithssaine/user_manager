package com.example.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/")
public class UserController {


    @GetMapping("/")
    public String test() {
        System.out.println("TEST ENDPOINT CALLED");
        return "API is working!";
    }

}
