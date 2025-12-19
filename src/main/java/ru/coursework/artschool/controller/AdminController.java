package ru.coursework.artschool.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.coursework.artschool.model.Role;
import ru.coursework.artschool.model.User;
import ru.coursework.artschool.model.UserRolesForm;
import ru.coursework.artschool.repository.RoleRepository;
import ru.coursework.artschool.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminController(UserRepository userRepository,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // Главная админ-панель
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("title", "Админ-панель");
        long userCount = userRepository.count();
        model.addAttribute("userCount", userCount);
        return "admin/index";
    }

    // Список пользователей
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("title", "Пользователи");
        model.addAttribute("users", users);
        return "admin/users";
    }

    // Форма редактирования ролей
    @GetMapping("/users/{id}/edit")
    public String editUserRoles(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        UserRolesForm form = new UserRolesForm();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());

        form.setAdmin(hasRole(user, "ROLE_ADMIN"));
        form.setTeacher(hasRole(user, "ROLE_TEACHER"));
        form.setStudent(hasRole(user, "ROLE_STUDENT"));

        model.addAttribute("title", "Роли пользователя");
        model.addAttribute("userRolesForm", form);

        return "admin/user-roles-form";
    }

    @PostMapping("/users/{id}")
    public String updateUserRoles(@PathVariable Long id,
                                  @ModelAttribute("userRolesForm") UserRolesForm form) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Set<Role> newRoles = new HashSet<>();

        if (form.isAdmin()) {
            newRoles.add(getOrCreateRole("ROLE_ADMIN"));
        }
        if (form.isTeacher()) {
            newRoles.add(getOrCreateRole("ROLE_TEACHER"));
        }
        if (form.isStudent()) {
            newRoles.add(getOrCreateRole("ROLE_STUDENT"));
        }

        // простая защита: не даём снять все роли у самого себя
        // (чтоб админ случайно не лишил себя прав)
        // по желанию можно усложнить
        user.setRoles(newRoles);
        userRepository.save(user);

        return "redirect:/admin/users";
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(name);
                    return roleRepository.save(r);
                });
    }
}
