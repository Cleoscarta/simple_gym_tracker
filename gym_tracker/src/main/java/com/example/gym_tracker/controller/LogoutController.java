package com.example.gym_tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    @GetMapping("/logout")
    public String logout() {
        // Tu możesz dodatkowo usunąć dane sesji, jeśli je trzymasz ręcznie
        return "redirect:/login";
    }
}
