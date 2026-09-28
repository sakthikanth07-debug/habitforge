package com.personalhabitstreaktracker.habitforge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/add-habits")
    public String addHabit() {
        return "add-habits";
    }

    @GetMapping("/habits-page")
    public String habits() {
        return "habits";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}