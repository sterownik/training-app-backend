package com.example.training.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class SpaController {
    @GetMapping({
            "/",
            "/welcome",
            "/login",
            "/dashboard",
            "/**/{path:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
