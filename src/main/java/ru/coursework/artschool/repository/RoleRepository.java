package ru.coursework.artschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.artschool.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
