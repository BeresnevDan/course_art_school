package ru.coursework.artschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.artschool.model.Group;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    boolean existsByCode(String code);

    List<Group> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String namePart, String codePart);
}
