package ru.coursework.artschool.controller;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.coursework.artschool.model.RegistrationForm;
import ru.coursework.artschool.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        // если уже залогинен — перенаправим на профиль
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/profile";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        model.addAttribute("title", "Регистрация");
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                                      BindingResult bindingResult,
                                      Model model) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Пароли не совпадают");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Регистрация");
            return "register";
        }

        try {
            userService.registerUser(
                    form.getUsername(),
                    form.getPassword(),
                    form.getFullName(),
                    form.getEmail()
            );
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("username", "user.exists", ex.getMessage());
            model.addAttribute("title", "Регистрация");
            return "register";
        }

        // после успешной регистрации перекидываем на /login
        return "redirect:/admin/users";
    }
}
