package ru.coursework.artschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.Group;
import ru.coursework.artschool.model.Student;
import ru.coursework.artschool.model.StudentForm;
import ru.coursework.artschool.repository.GroupRepository;
import ru.coursework.artschool.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final GroupRepository groupRepository;

    public StudentService(StudentRepository studentRepository,
                          GroupRepository groupRepository) {
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> search(String query, Long groupId) {
        boolean hasGroup = groupId != null;

        // нет поисковой строки → фильтруем только по группе
        if (query == null || query.isBlank()) {
            return hasGroup
                    ? studentRepository.findByGroup_Id(groupId)
                    : studentRepository.findAll();
        }

        // нормализуем запрос
        String normalized = query.trim().toLowerCase();
        String[] parts = normalized.split("\\s+"); // разбиваем по пробелам

        // загружаем всех учеников (оптимизируем при необходимости)
        List<Student> baseList = hasGroup
                ? studentRepository.findByGroup_Id(groupId)
                : studentRepository.findAll();

        // фильтрация
        return baseList.stream()
                .filter(student -> {
                    String name = student.getFullName().toLowerCase();

                    // каждое слово из поиска должно встречаться в ФИО
                    for (String p : parts) {
                        if (!name.contains(p)) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
    }



    @Transactional(readOnly = true)
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    @Transactional
    public Student createFromForm(StudentForm form) {
        Student student = new Student();
        applyFormToEntity(form, student);
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateFromForm(Long id, StudentForm form) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ученик не найден"));

        applyFormToEntity(form, existing);
        return studentRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public StudentForm toForm(Student student) {
        StudentForm form = new StudentForm();
        form.setId(student.getId());
        form.setFullName(student.getFullName());
        form.setDateOfBirth(student.getDateOfBirth());
        form.setEmail(student.getEmail());
        form.setPhone(student.getPhone());
        form.setAdmissionYear(student.getAdmissionYear());
        form.setNotes(student.getNotes());
        if (student.getGroup() != null) {
            form.setGroupId(student.getGroup().getId());
        }
        return form;
    }

    private void applyFormToEntity(StudentForm form, Student student) {
        student.setFullName(form.getFullName());
        student.setDateOfBirth(form.getDateOfBirth());
        student.setEmail(form.getEmail());
        student.setPhone(form.getPhone());
        student.setAdmissionYear(form.getAdmissionYear());
        student.setNotes(form.getNotes());

        Group group = null;
        if (form.getGroupId() != null) {
            group = groupRepository.findById(form.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));
        }
        student.setGroup(group);
    }
}
