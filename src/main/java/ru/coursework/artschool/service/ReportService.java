package ru.coursework.artschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.Grade;
import ru.coursework.artschool.model.Group;
import ru.coursework.artschool.model.Subject;
import ru.coursework.artschool.model.Student;
import ru.coursework.artschool.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final StudentRepository studentRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final GradeRepository gradeRepository;
    private final TeacherRepository teacherRepository;

    public ReportService(StudentRepository studentRepository,
                         GroupRepository groupRepository,
                         SubjectRepository subjectRepository,
                         LessonRepository lessonRepository,
                         GradeRepository gradeRepository,
                         TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
        this.gradeRepository = gradeRepository;
        this.teacherRepository = teacherRepository;
    }

    // ===== DTO для отчётов =====

    public static class OverviewStats {
        private long totalStudents;
        private long totalTeachers;
        private long totalGroups;
        private long totalSubjects;
        private long totalLessons;
        private Double overallAverageGrade;

        public long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

        public long getTotalTeachers() { return totalTeachers; }
        public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }

        public long getTotalGroups() { return totalGroups; }
        public void setTotalGroups(long totalGroups) { this.totalGroups = totalGroups; }

        public long getTotalSubjects() { return totalSubjects; }
        public void setTotalSubjects(long totalSubjects) { this.totalSubjects = totalSubjects; }

        public long getTotalLessons() { return totalLessons; }
        public void setTotalLessons(long totalLessons) { this.totalLessons = totalLessons; }

        public Double getOverallAverageGrade() { return overallAverageGrade; }
        public void setOverallAverageGrade(Double overallAverageGrade) { this.overallAverageGrade = overallAverageGrade; }
    }

    public static class GroupStudentStats {
        private Group group;
        private long studentCount;
        private Double averageGrade; // средний балл по группе (по всем предметам)

        public Group getGroup() { return group; }
        public void setGroup(Group group) { this.group = group; }

        public long getStudentCount() { return studentCount; }
        public void setStudentCount(long studentCount) { this.studentCount = studentCount; }

        public Double getAverageGrade() { return averageGrade; }
        public void setAverageGrade(Double averageGrade) { this.averageGrade = averageGrade; }
    }

    public static class SubjectAverageStats {
        private Subject subject;
        private Double averageGrade;

        public Subject getSubject() { return subject; }
        public void setSubject(Subject subject) { this.subject = subject; }

        public Double getAverageGrade() { return averageGrade; }
        public void setAverageGrade(Double averageGrade) { this.averageGrade = averageGrade; }
    }

    // ===== Методы отчётов =====

    @Transactional(readOnly = true)
    public OverviewStats getOverviewStats() {
        OverviewStats stats = new OverviewStats();

        stats.setTotalStudents(studentRepository.count());
        stats.setTotalTeachers(teacherRepository.count());
        stats.setTotalGroups(groupRepository.count());
        stats.setTotalSubjects(subjectRepository.count());
        stats.setTotalLessons(lessonRepository.count());

        List<Grade> grades = gradeRepository.findAll();
        if (!grades.isEmpty()) {
            double avg = grades.stream()
                    .mapToInt(Grade::getValue)
                    .average()
                    .orElse(0.0);
            stats.setOverallAverageGrade(avg);
        } else {
            stats.setOverallAverageGrade(null);
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public List<GroupStudentStats> getGroupStudentStats() {
        List<Group> groups = groupRepository.findAll();
        List<Student> students = studentRepository.findAll();
        List<Grade> grades = gradeRepository.findAll();

        // распределяем студентов по группам в памяти
        Map<Long, Long> studentCountByGroup = students.stream()
                .filter(s -> s.getGroup() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getGroup().getId(),
                        Collectors.counting()
                ));

        // собираем оценки по группе
        Map<Long, IntSummaryStatistics> gradeStatsByGroup = new HashMap<>();
        for (Grade g : grades) {
            if (g.getLesson() == null || g.getLesson().getGroup() == null) continue;
            Long groupId = g.getLesson().getGroup().getId();
            gradeStatsByGroup
                    .computeIfAbsent(groupId, id -> new IntSummaryStatistics())
                    .accept(g.getValue());
        }

        List<GroupStudentStats> result = new ArrayList<>();

        for (Group group : groups) {
            GroupStudentStats s = new GroupStudentStats();
            s.setGroup(group);
            s.setStudentCount(studentCountByGroup.getOrDefault(group.getId(), 0L));

            IntSummaryStatistics stat = gradeStatsByGroup.get(group.getId());
            if (stat != null && stat.getCount() > 0) {
                s.setAverageGrade(stat.getAverage());
            } else {
                s.setAverageGrade(null);
            }

            result.add(s);
        }

        // сортируем по коду группы
        result.sort(Comparator.comparing(g -> g.getGroup().getCode(), String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    @Transactional(readOnly = true)
    public List<SubjectAverageStats> getSubjectAverageStats() {
        List<Grade> grades = gradeRepository.findAll();

        // subjectId -> stats
        Map<Long, IntSummaryStatistics> statsBySubject = new HashMap<>();
        Map<Long, Subject> subjectById = new HashMap<>();

        for (Grade g : grades) {
            if (g.getLesson() == null || g.getLesson().getSubject() == null) continue;
            Subject subject = g.getLesson().getSubject();
            Long subjectId = subject.getId();

            subjectById.putIfAbsent(subjectId, subject);
            statsBySubject
                    .computeIfAbsent(subjectId, id -> new IntSummaryStatistics())
                    .accept(g.getValue());
        }

        List<SubjectAverageStats> result = new ArrayList<>();

        for (Map.Entry<Long, IntSummaryStatistics> e : statsBySubject.entrySet()) {
            Subject subj = subjectById.get(e.getKey());
            if (subj == null) continue;
            IntSummaryStatistics st = e.getValue();

            SubjectAverageStats sa = new SubjectAverageStats();
            sa.setSubject(subj);
            sa.setAverageGrade(st.getCount() > 0 ? st.getAverage() : null);
            result.add(sa);
        }

        // сортируем по коду предмета
        result.sort(Comparator.comparing(s -> s.getSubject().getCode(), String.CASE_INSENSITIVE_ORDER));
        return result;
    }
}
