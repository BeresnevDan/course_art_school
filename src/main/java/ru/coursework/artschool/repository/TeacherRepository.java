package ru.coursework.artschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.artschool.model.Teacher;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    List<Teacher> findByFullNameContainingIgnoreCaseOrSpecializationContainingIgnoreCase(
            String namePart, String specializationPart
    );
}
