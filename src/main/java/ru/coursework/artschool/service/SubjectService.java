package ru.coursework.artschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.Subject;
import ru.coursework.artschool.repository.SubjectRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Subject> search(String query) {
        if (query == null || query.isBlank()) {
            return subjectRepository.findAll();
        }
        String q = query.trim();
        return subjectRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(q, q);
    }

    @Transactional(readOnly = true)
    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    @Transactional
    public Subject create(Subject subject) {
        // проверяем уникальность кода
        if (subjectRepository.existsByCode(subject.getCode())) {
            throw new IllegalArgumentException("Дисциплина с таким кодом уже существует");
        }
        return subjectRepository.save(subject);
    }

    @Transactional
    public Subject update(Long id, Subject form) {
        Subject existing = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Дисциплина не найдена"));

        // если код меняется — проверяем уникальность
        if (!existing.getCode().equals(form.getCode())
                && subjectRepository.existsByCode(form.getCode())) {
            throw new IllegalArgumentException("Дисциплина с таким кодом уже существует");
        }

        existing.setCode(form.getCode());
        existing.setName(form.getName());
        existing.setDescription(form.getDescription());
        existing.setHours(form.getHours());

        return subjectRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        subjectRepository.deleteById(id);
    }
}
