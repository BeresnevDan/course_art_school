package ru.coursework.artschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.Teacher;
import ru.coursework.artschool.repository.TeacherRepository;

import java.util.List;
import java.util.Optional;

import ru.coursework.artschool.model.TeacherForm;
import ru.coursework.artschool.model.Subject;
import ru.coursework.artschool.repository.SubjectRepository;

import java.util.HashSet;
import java.util.Set;


@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;

    public TeacherService(TeacherRepository teacherRepository,
                          SubjectRepository subjectRepository) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
    }


    @Transactional(readOnly = true)
    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Teacher> search(String query) {
        if (query == null || query.isBlank()) {
            return teacherRepository.findAll();
        }
        String q = query.trim();
        return teacherRepository
                .findByFullNameContainingIgnoreCaseOrSpecializationContainingIgnoreCase(q, q);
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> findById(Long id) {
        return teacherRepository.findById(id);
    }

    @Transactional
    public Teacher createFromForm(TeacherForm form) {
        Teacher teacher = new Teacher();
        applyFormToEntity(form, teacher);
        return teacherRepository.save(teacher);
    }

    @Transactional
    public Teacher updateFromForm(Long id, TeacherForm form) {
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));

        applyFormToEntity(form, existing);
        return teacherRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public TeacherForm toForm(Teacher teacher) {
        TeacherForm form = new TeacherForm();
        form.setId(teacher.getId());
        form.setFullName(teacher.getFullName());
        form.setSpecialization(teacher.getSpecialization());
        form.setEmail(teacher.getEmail());
        form.setPhone(teacher.getPhone());
        form.setExperienceYears(teacher.getExperienceYears());
        form.setBio(teacher.getBio());

        // заполняем выбранные дисциплины
        if (teacher.getSubjects() != null) {
            form.setSubjectIds(
                    teacher.getSubjects()
                            .stream()
                            .map(Subject::getId)
                            .toList()
            );
        }

        return form;
    }

    private void applyFormToEntity(TeacherForm form, Teacher teacher) {
        teacher.setFullName(form.getFullName());
        teacher.setSpecialization(form.getSpecialization());
        teacher.setEmail(form.getEmail());
        teacher.setPhone(form.getPhone());
        teacher.setExperienceYears(form.getExperienceYears());
        teacher.setBio(form.getBio());

        // обновляем привязку к дисциплинам
        Set<Subject> subjects = new HashSet<>();
        if (form.getSubjectIds() != null && !form.getSubjectIds().isEmpty()) {
            for (Long subjectId : form.getSubjectIds()) {
                Subject subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new IllegalArgumentException("Дисциплина не найдена"));
                subjects.add(subject);
            }
        }
        teacher.setSubjects(subjects);
    }

    @Transactional
    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }
}
