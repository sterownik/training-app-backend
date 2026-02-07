package com.example.training.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping({
            "/",
            "/welcome",
            "/login",
            "/media/Boathouse-Bold-MRRVS7RL.woff2",
            "/media/Boathouse-Regular-WBB34DB6.woff2",
            "/dashboard",
            "/**/{path:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
