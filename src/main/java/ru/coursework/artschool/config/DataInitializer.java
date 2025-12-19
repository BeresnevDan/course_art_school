package ru.coursework.artschool.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.Role;
import ru.coursework.artschool.model.Room;
import ru.coursework.artschool.model.User;
import ru.coursework.artschool.repository.RoleRepository;
import ru.coursework.artschool.repository.RoomRepository;
import ru.coursework.artschool.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      RoomRepository roomRepository) {
        return args -> {

            // ===== РОЛИ =====
            Role adminRole = ensureRole(roleRepository, "ROLE_ADMIN");
            Role userRole = ensureRole(roleRepository, "ROLE_USER");
            Role teacherRole = ensureRole(roleRepository, "ROLE_TEACHER");
            Role studentRole = ensureRole(roleRepository, "ROLE_STUDENT");

            // ===== ПОЛЬЗОВАТЕЛИ =====

            // Администратор
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setFullName("Администратор системы");
                admin.setEmail("admin@example.com");

                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                roles.add(userRole);
                // при желании можно оставить и teacherRole, чтобы админ был ещё и как преподаватель
                // roles.add(teacherRole);

                admin.setRoles(roles);
                userRepository.save(admin);

                System.out.println("Создан пользователь admin/admin");
            }

            // Преподаватель
            if (userRepository.findByUsername("teacher").isEmpty()) {
                User teacher = new User();
                teacher.setUsername("teacher");
                teacher.setPassword(passwordEncoder.encode("teacher"));
                teacher.setFullName("Иванов Иван Иванович");
                teacher.setEmail("teacher@example.com");

                Set<Role> roles = new HashSet<>();
                roles.add(teacherRole);
                roles.add(userRole);

                teacher.setRoles(roles);
                userRepository.save(teacher);

                System.out.println("Создан пользователь teacher/teacher");
            }

            // Студент
            if (userRepository.findByUsername("student").isEmpty()) {
                User student = new User();
                student.setUsername("student");
                student.setPassword(passwordEncoder.encode("student"));
                student.setFullName("Петров Пётр Петрович");
                student.setEmail("student@example.com");

                Set<Role> roles = new HashSet<>();
                roles.add(studentRole);
                roles.add(userRole);

                student.setRoles(roles);
                userRepository.save(student);

                System.out.println("Создан пользователь student/student");
            }

            // ===== АУДИТОРИИ =====
            if (roomRepository.count() == 0) {
                Room r1 = new Room();
                r1.setCode("Ауд. 101");
                r1.setCapacity(12);
                r1.setDescription("Живопись, базовая аудитория");

                Room r2 = new Room();
                r2.setCode("Мастерская 1");
                r2.setCapacity(8);
                r2.setDescription("Мастерская для скульптуры");

                roomRepository.save(r1);
                roomRepository.save(r2);

                System.out.println("Инициализированы аудитории: Ауд. 101, Мастерская 1");
            }
        };
    }

    private Role ensureRole(RoleRepository roleRepository, String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }
}
