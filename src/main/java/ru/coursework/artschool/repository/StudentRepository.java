package ru.coursework.artschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.artschool.model.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByFullNameContainingIgnoreCase(String part1);


    List<Student> findByGroup_Id(Long groupId);

    List<Student> findByFullNameContainingIgnoreCaseAndGroup_Id(String namePart, Long groupId);

}
