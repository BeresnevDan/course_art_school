package ru.coursework.artschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.artschool.model.Grade;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    // для конкретной группы и предмета — все оценки
    List<Grade> findByLesson_Group_IdAndLesson_Subject_Id(Long groupId, Long subjectId);
    List<Grade> findByStudent_Id(Long studentId);

    // оценка конкретного ученика за конкретное занятие
    Optional<Grade> findByStudent_IdAndLesson_Id(Long studentId, Long lessonId);


}
