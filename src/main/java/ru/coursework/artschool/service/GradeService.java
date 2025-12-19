package ru.coursework.artschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.*;
import ru.coursework.artschool.repository.*;
import ru.coursework.artschool.model.Subject;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;

    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        LessonRepository lessonRepository) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    public Grade createFromForm(GradeForm form) {
        Student student = studentRepository.findById(form.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Ученик не найден"));
        Lesson lesson = lessonRepository.findById(form.getLessonId())
                .orElseThrow(() -> new IllegalArgumentException("Занятие не найдено"));

        // проверка: ученик должен принадлежать группе занятия
        if (student.getGroup() == null ||
                !student.getGroup().getId().equals(lesson.getGroup().getId())) {
            throw new IllegalArgumentException("Ученик не относится к группе этого занятия");
        }

        // проверка: нет другой оценки для этого ученика за это занятие
        if (gradeRepository.findByStudent_IdAndLesson_Id(student.getId(), lesson.getId()).isPresent()) {
            throw new IllegalArgumentException("Для этого ученика за это занятие оценка уже существует");
        }

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setLesson(lesson);
        grade.setValue(form.getValue());
        grade.setComment(form.getComment());

        return gradeRepository.save(grade);
    }

    @Transactional
    public Grade updateFromForm(Long id, GradeForm form) {
        Grade existing = gradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Оценка не найдена"));

        existing.setValue(form.getValue());
        existing.setComment(form.getComment());

        return gradeRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        gradeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Grade> findById(Long id) {
        return gradeRepository.findById(id);
    }

    // ===== Данные для журнала по группе и предмету =====

    @Transactional(readOnly = true)
    public JournalData getJournalDataForGroupAndSubject(
            Group group,
            Subject subject,
            LocalDate fromDate,
            LocalDate toDate,
            List<Student> students) {

        List<Lesson> lessons;

        if (fromDate != null && toDate != null) {
            lessons = lessonRepository
                    .findByGroup_IdAndSubject_IdAndDateBetweenOrderByDateAscStartTimeAsc(
                            group.getId(),
                            subject.getId(),
                            fromDate,
                            toDate
                    );
        } else {
            lessons = lessonRepository
                    .findByGroup_IdAndSubject_IdOrderByDateAscStartTimeAsc(
                            group.getId(),
                            subject.getId()
                    );
        }

        List<Grade> grades = gradeRepository
                .findByLesson_Group_IdAndLesson_Subject_Id(group.getId(), subject.getId());

        // индекс по (studentId, lessonId)
        Map<Long, Map<Long, Grade>> gradeByStudentThenLesson = new HashMap<>();

        for (Grade g : grades) {
            Long sId = g.getStudent().getId();
            Long lId = g.getLesson().getId();

            gradeByStudentThenLesson
                    .computeIfAbsent(sId, k -> new HashMap<>())
                    .put(lId, g);
        }

        return new JournalData(lessons, students, gradeByStudentThenLesson);
    }

    // DTO-контейнер для журнала
    public static class JournalData {
        private final List<Lesson> lessons;
        private final List<Student> students;
        private final Map<Long, Map<Long, Grade>> grades;

        public JournalData(List<Lesson> lessons,
                           List<Student> students,
                           Map<Long, Map<Long, Grade>> grades) {
            this.lessons = lessons;
            this.students = students;
            this.grades = grades;
        }

        public List<Lesson> getLessons() {
            return lessons;
        }

        public List<Student> getStudents() {
            return students;
        }

        public Map<Long, Map<Long, Grade>> getGrades() {
            return grades;
        }

        public Grade getGradeFor(Long studentId, Long lessonId) {
            Map<Long, Grade> byLesson = grades.get(studentId);
            if (byLesson == null) return null;
            return byLesson.get(lessonId);
        }
    }
    // ===== Успеваемость одного ученика =====

    public static class SubjectStats {
        private Subject subject;
        private long count;
        private int sum;

        public Subject getSubject() {
            return subject;
        }

        public void setSubject(Subject subject) {
            this.subject = subject;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public int getSum() {
            return sum;
        }

        public void setSum(int sum) {
            this.sum = sum;
        }

        public double getAverage() {
            return count == 0 ? 0.0 : (double) sum / count;
        }
    }

    public static class StudentPerformance {
        private final List<Grade> grades;
        private final List<SubjectStats> subjectStats;
        private final Double overallAverage;

        public StudentPerformance(List<Grade> grades, List<SubjectStats> subjectStats, Double overallAverage) {
            this.grades = grades;
            this.subjectStats = subjectStats;
            this.overallAverage = overallAverage;
        }

        public List<Grade> getGrades() {
            return grades;
        }

        public List<SubjectStats> getSubjectStats() {
            return subjectStats;
        }

        public Double getOverallAverage() {
            return overallAverage;
        }
    }

    @Transactional(readOnly = true)
    public StudentPerformance getStudentPerformance(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudent_Id(studentId);

        // сортируем по дате и времени занятия
        grades.sort(
                java.util.Comparator
                        .comparing((Grade g) -> g.getLesson().getDate())
                        .thenComparing(g -> g.getLesson().getStartTime())
        );

        java.util.Map<Long, SubjectStats> statsMap = new java.util.LinkedHashMap<>();

        int totalSum = 0;
        long totalCount = 0;

        for (Grade g : grades) {
            Subject subject = g.getLesson().getSubject();
            Long subjectId = subject.getId();

            SubjectStats stats = statsMap.get(subjectId);
            if (stats == null) {
                stats = new SubjectStats();
                stats.setSubject(subject);
                statsMap.put(subjectId, stats);
            }

            stats.setCount(stats.getCount() + 1);
            stats.setSum(stats.getSum() + g.getValue());

            totalCount++;
            totalSum += g.getValue();
        }

        Double overallAverage = (totalCount == 0)
                ? null
                : (double) totalSum / totalCount;

        return new StudentPerformance(
                grades,
                new java.util.ArrayList<>(statsMap.values()),
                overallAverage
        );
    }

}
