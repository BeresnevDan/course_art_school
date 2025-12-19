package ru.coursework.artschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.Group;
import ru.coursework.artschool.model.GroupForm;
import ru.coursework.artschool.model.Teacher;
import ru.coursework.artschool.repository.GroupRepository;
import ru.coursework.artschool.repository.TeacherRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final TeacherRepository teacherRepository;

    public GroupService(GroupRepository groupRepository,
                        TeacherRepository teacherRepository) {
        this.groupRepository = groupRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional(readOnly = true)
    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Group> search(String query) {
        if (query == null || query.isBlank()) {
            return groupRepository.findAll();
        }
        String q = query.trim();
        return groupRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(q, q);
    }

    @Transactional(readOnly = true)
    public Optional<Group> findById(Long id) {
        return groupRepository.findById(id);
    }

    @Transactional
    public Group createFromForm(GroupForm form) {
        if (groupRepository.existsByCode(form.getCode())) {
            throw new IllegalArgumentException("Группа с таким кодом уже существует");
        }

        Group group = new Group();
        applyFormToEntity(form, group);

        return groupRepository.save(group);
    }

    @Transactional
    public Group updateFromForm(Long id, GroupForm form) {
        Group existing = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

        if (!existing.getCode().equals(form.getCode())
                && groupRepository.existsByCode(form.getCode())) {
            throw new IllegalArgumentException("Группа с таким кодом уже существует");
        }

        applyFormToEntity(form, existing);
        return groupRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        groupRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public GroupForm toForm(Group group) {
        GroupForm form = new GroupForm();
        form.setId(group.getId());
        form.setCode(group.getCode());
        form.setName(group.getName());
        form.setAdmissionYear(group.getAdmissionYear());
        form.setLevel(group.getLevel());
        form.setDescription(group.getDescription());
        if (group.getCurator() != null) {
            form.setCuratorId(group.getCurator().getId());
        }
        return form;
    }

    private void applyFormToEntity(GroupForm form, Group group) {
        group.setCode(form.getCode());
        group.setName(form.getName());
        group.setAdmissionYear(form.getAdmissionYear());
        group.setLevel(form.getLevel());
        group.setDescription(form.getDescription());

        Teacher curator = null;
        if (form.getCuratorId() != null) {
            curator = teacherRepository.findById(form.getCuratorId())
                    .orElseThrow(() -> new IllegalArgumentException("Куратор не найден"));
        }
        group.setCurator(curator);
    }
}
