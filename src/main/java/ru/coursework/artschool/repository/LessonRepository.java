package ru.coursework.artschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.artschool.model.Lesson;

import java.time.LocalDate;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByDate(LocalDate date);

    List<Lesson> findByDateBetween(LocalDate start, LocalDate end);

    List<Lesson> findByDateAndTeacher_Id(LocalDate date, Long teacherId);

    List<Lesson> findByDateAndGroup_Id(LocalDate date, Long groupId);

    List<Lesson> findByDateAndRoom_Id(LocalDate date, Long roomId);

    List<Lesson> findByGroup_IdAndSubject_IdOrderByDateAscStartTimeAsc(Long groupId, Long subjectId);

    List<Lesson> findByGroup_IdAndSubject_IdAndDateBetweenOrderByDateAscStartTimeAsc(
            Long groupId, Long subjectId,
            java.time.LocalDate start,
            java.time.LocalDate end
    );
}
