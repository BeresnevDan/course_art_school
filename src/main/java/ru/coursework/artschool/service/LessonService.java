package ru.coursework.artschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.*;
import ru.coursework.artschool.repository.*;
import ru.coursework.artschool.model.LessonForm;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    private static final Duration LESSON_DURATION = Duration.ofMinutes(90);
    private static final LocalTime LUNCH_START = LocalTime.of(13, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(14, 0);

    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final GroupRepository groupRepository;
    private final TeacherRepository teacherRepository;
    private final RoomRepository roomRepository;

    public LessonService(LessonRepository lessonRepository,
                         SubjectRepository subjectRepository,
                         GroupRepository groupRepository,
                         TeacherRepository teacherRepository,
                         RoomRepository roomRepository) {
        this.lessonRepository = lessonRepository;
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
        this.teacherRepository = teacherRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findById(Long id) {
        return lessonRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Lesson> findByDate(LocalDate date) {
        List<Lesson> lessons = lessonRepository.findByDate(date);
        lessons.sort(Comparator.comparing(Lesson::getStartTime));
        return lessons;
    }

    @Transactional(readOnly = true)
    public List<Lesson> findByDateRange(LocalDate start, LocalDate end) {
        List<Lesson> lessons = lessonRepository.findByDateBetween(start, end);
        lessons.sort(Comparator
                .comparing(Lesson::getDate)
                .thenComparing(Lesson::getStartTime));
        return lessons;
    }

    @Transactional
    public Lesson create(Lesson lesson, Long subjectId, Long groupId,
                         Long teacherId, Long roomId) {

        fillRelations(lesson, subjectId, groupId, teacherId, roomId);
        validateLesson(lesson, null);

        return lessonRepository.save(lesson);
    }

    @Transactional
    public Lesson update(Long id, Lesson form, Long subjectId, Long groupId,
                         Long teacherId, Long roomId) {

        Lesson existing = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Занятие не найдено"));

        existing.setDate(form.getDate());
        existing.setStartTime(form.getStartTime());
        existing.setEndTime(form.getEndTime());
        existing.setType(form.getType());
        existing.setNotes(form.getNotes());

        fillRelations(existing, subjectId, groupId, teacherId, roomId);
        validateLesson(existing, id);

        return lessonRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<Lesson> findByGroupAndSubject(Long groupId, Long subjectId) {
        return lessonRepository
                .findByGroup_IdAndSubject_IdOrderByDateAscStartTimeAsc(groupId, subjectId);
    }


    @Transactional
    public void deleteById(Long id) {
        lessonRepository.deleteById(id);
    }

    private void fillRelations(Lesson lesson, Long subjectId, Long groupId,
                               Long teacherId, Long roomId) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Дисциплина не найдена"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Аудитория не найдена"));

        lesson.setSubject(subject);
        lesson.setGroup(group);
        lesson.setTeacher(teacher);
        lesson.setRoom(room);
    }

    private void validateLesson(Lesson lesson, Long currentLessonId) {
        if (lesson.getDate() == null || lesson.getStartTime() == null || lesson.getEndTime() == null) {
            throw new IllegalArgumentException("Дата и время занятия обязательны");
        }

        // 1) Длительность строго 90 минут
        Duration duration = Duration.between(lesson.getStartTime(), lesson.getEndTime());
        if (!duration.equals(LESSON_DURATION)) {
            throw new IllegalArgumentException("Занятие должно длиться ровно 1 час 30 минут");
        }

        // 2) Не пересекается с обедом (13:00–14:00)
        if (isOverlaps(lesson.getStartTime(), lesson.getEndTime(), LUNCH_START, LUNCH_END)) {
            throw new IllegalArgumentException("Занятие не может пересекаться с обедом (13:00–14:00)");
        }

        // 3) Проверка пересечений по учителю, группе, аудитории
        LocalDate date = lesson.getDate();
        LocalTime start = lesson.getStartTime();
        LocalTime end = lesson.getEndTime();

        List<Lesson> sameDayLessons = lessonRepository.findByDate(date);

        for (Lesson other : sameDayLessons) {
            if (currentLessonId != null && other.getId().equals(currentLessonId)) {
                continue; // пропускаем само себя при обновлении
            }

            LocalTime otherStart = other.getStartTime();
            LocalTime otherEnd = other.getEndTime();

            boolean overlap = isOverlaps(start, end, otherStart, otherEnd);
            if (!overlap) {
                continue;
            }

            // тот же преподаватель
            if (other.getTeacher().getId().equals(lesson.getTeacher().getId())) {
                throw new IllegalArgumentException(
                        "Преподаватель уже занят в это время (" +
                                otherStart + "–" + otherEnd + ")");
            }

            // та же группа
            if (other.getGroup().getId().equals(lesson.getGroup().getId())) {
                throw new IllegalArgumentException(
                        "У группы уже есть занятие в это время (" +
                                otherStart + "–" + otherEnd + ")");
            }

            // тот же кабинет
            if (other.getRoom().getId().equals(lesson.getRoom().getId())) {
                throw new IllegalArgumentException(
                        "Аудитория уже занята в это время (" +
                                otherStart + "–" + otherEnd + ")");
            }
        }
    }

    private boolean isOverlaps(LocalTime start1, LocalTime end1,
                               LocalTime start2, LocalTime end2) {
        // пересекаются, если один интервал начинается до конца другого и заканчивается после начала
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    @Transactional(readOnly = true)
    public LessonForm toForm(Lesson lesson) {
        LessonForm form = new LessonForm();
        form.setId(lesson.getId());
        form.setDate(lesson.getDate());
        form.setStartTime(lesson.getStartTime());
        form.setEndTime(lesson.getEndTime());
        form.setType(lesson.getType());
        form.setNotes(lesson.getNotes());
        form.setSubjectId(lesson.getSubject().getId());
        form.setGroupId(lesson.getGroup().getId());
        form.setTeacherId(lesson.getTeacher().getId());
        form.setRoomId(lesson.getRoom().getId());
        return form;
    }

}
