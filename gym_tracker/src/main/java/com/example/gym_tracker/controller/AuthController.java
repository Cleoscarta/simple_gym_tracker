package com.example.gym_tracker.controller;

import com.example.gym_tracker.model.User;
import com.example.gym_tracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute User user, Model model, HttpSession session) {
        return userRepository.findByLogin(user.getLogin())
                .map(dbUser -> {
                    if (dbUser.getPassword().equals(user.getPassword())) {
                        session.setAttribute("loggedUser", dbUser.getLogin()); // zapisz login do sesji
                        return "redirect:/dashboard";
                    } else {
                        model.addAttribute("error", "Błędne hasło");
                        return "login";
                    }
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Nie ma takiego użytkownika");
                    return "login";
                });
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        if (userRepository.findByLogin(user.getLogin()).isPresent()) {
            model.addAttribute("error", "Login jest już zajęty");
            return "register";
        }

        userRepository.save(user);
        model.addAttribute("message", "Zarejestrowano! Możesz się teraz zalogować.");
        return "login";
    }
}
