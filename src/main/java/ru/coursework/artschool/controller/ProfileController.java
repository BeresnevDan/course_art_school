package ru.coursework.artschool.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.coursework.artschool.model.User;
import ru.coursework.artschool.repository.UserRepository;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal == null) {
            // на всякий случай, но с PreAuthorize сюда аноним не попадёт
            return "redirect:/login";
        }

        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));

        model.addAttribute("title", "Профиль");
        model.addAttribute("user", user);

        return "profile";
    }
}
