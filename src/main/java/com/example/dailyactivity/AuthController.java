package com.example.dailyactivity;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        username = username.trim();

        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute(
                    "error",
                    "Логин и пароль не должны быть пустыми"
            );
            return "register";
        }

        if (userRepository.existsByUsername(username)) {
            model.addAttribute(
                    "error",
                    "Такой логин уже существует"
            );
            return "register";
        }

        User user = new User(
                username,
                passwordEncoder.encode(password)
        );

        user.setDisplayName(username);

        userRepository.save(user);

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String registered,
            Model model) {

        if (error != null) {
            model.addAttribute(
                    "error",
                    "Неверный логин или пароль"
            );
        }

        if (registered != null) {
            model.addAttribute(
                    "success",
                    "Регистрация прошла успешно. Теперь войдите."
            );
        }

        return "login";
    }
}