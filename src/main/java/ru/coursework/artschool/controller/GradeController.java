package ru.coursework.artschool.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.coursework.artschool.model.*;
import ru.coursework.artschool.service.*;

import java.util.List;

@Controller
@RequestMapping("/grades")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class GradeController {

    private final GradeService gradeService;
    private final GroupService groupService;
    private final SubjectService subjectService;
    private final StudentService studentService;
    private final LessonService lessonService;

    public GradeController(GradeService gradeService,
                           GroupService groupService,
                           SubjectService subjectService,
                           StudentService studentService,
                           LessonService lessonService) {
        this.gradeService = gradeService;
        this.groupService = groupService;
        this.subjectService = subjectService;
        this.studentService = studentService;
        this.lessonService = lessonService;
    }

    // Форма добавления оценки
    @GetMapping("/new")
    public String newGradeForm(@RequestParam Long groupId,
                               @RequestParam Long subjectId,
                               @RequestParam(required = false) Long studentId,
                               @RequestParam(required = false) Long lessonId,
                               Model model) {

        Group group = groupService.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));
        Subject subject = subjectService.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Дисциплина не найдена"));

        GradeForm form = new GradeForm();
        form.setGroupId(groupId);
        form.setSubjectId(subjectId);
        form.setStudentId(studentId);
        form.setLessonId(lessonId);

        model.addAttribute("title", "Добавление оценки");
        model.addAttribute("gradeForm", form);
        model.addAttribute("group", group);
        model.addAttribute("subject", subject);

        // список учеников группы
        model.addAttribute("students", studentService.search(null, groupId));

        // занятия по этой группе и предмету
        model.addAttribute("lessons",
                lessonService.findByGroupAndSubject(groupId, subjectId)
        );


        return "grade-form";
    }

    // Обработка добавления
    @PostMapping
    public String createGrade(@Valid @ModelAttribute("gradeForm") GradeForm form,
                              BindingResult bindingResult,
                              Model model) {

        Group group = groupService.findById(form.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));
        Subject subject = subjectService.findById(form.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Дисциплина не найдена"));

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Добавление оценки");
            model.addAttribute("group", group);
            model.addAttribute("subject", subject);
            model.addAttribute("students", studentService.search(null, form.getGroupId()));

            model.addAttribute("lessons",
                    lessonService.findByGroupAndSubject(form.getGroupId(), form.getSubjectId())
            );


            return "grade-form";
        }

        try {
            gradeService.createFromForm(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("grade.error", ex.getMessage());
            model.addAttribute("title", "Добавление оценки");
            model.addAttribute("group", group);
            model.addAttribute("subject", subject);
            model.addAttribute("students", studentService.search(null, form.getGroupId()));
            model.addAttribute("lessons",
                    lessonService.findByGroupAndSubject(form.getGroupId(), form.getSubjectId())
            );

            return "grade-form";
        }

        return "redirect:/journal?groupId=" + form.getGroupId() +
                "&subjectId=" + form.getSubjectId();
    }

    // Форма редактирования оценки
    @GetMapping("/{id}/edit")
    public String editGradeForm(@PathVariable Long id, Model model) {
        Grade grade = gradeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Оценка не найдена"));

        GradeForm form = new GradeForm();
        form.setId(grade.getId());
        form.setStudentId(grade.getStudent().getId());
        form.setLessonId(grade.getLesson().getId());
        form.setValue(grade.getValue());
        form.setComment(grade.getComment());

        Long groupId = grade.getLesson().getGroup().getId();
        Long subjectId = grade.getLesson().getSubject().getId();
        form.setGroupId(groupId);
        form.setSubjectId(subjectId);

        model.addAttribute("title", "Редактирование оценки");
        model.addAttribute("gradeForm", form);
        model.addAttribute("group", grade.getLesson().getGroup());
        model.addAttribute("subject", grade.getLesson().getSubject());
        model.addAttribute("students", List.of(grade.getStudent()));
        model.addAttribute("lessons", List.of(grade.getLesson()));
        model.addAttribute("isEdit", true);

        return "grade-form";
    }

    @PostMapping("/{id}")
    public String updateGrade(@PathVariable Long id,
                              @Valid @ModelAttribute("gradeForm") GradeForm form,
                              BindingResult bindingResult,
                              Model model) {

        Grade existing = gradeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Оценка не найдена"));

        Long groupId = existing.getLesson().getGroup().getId();
        Long subjectId = existing.getLesson().getSubject().getId();

        form.setGroupId(groupId);
        form.setSubjectId(subjectId);
        form.setStudentId(existing.getStudent().getId());
        form.setLessonId(existing.getLesson().getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Редактирование оценки");
            model.addAttribute("group", existing.getLesson().getGroup());
            model.addAttribute("subject", existing.getLesson().getSubject());
            model.addAttribute("students", List.of(existing.getStudent()));
            model.addAttribute("lessons", List.of(existing.getLesson()));
            model.addAttribute("isEdit", true);
            return "grade-form";
        }

        gradeService.updateFromForm(id, form);

        return "redirect:/journal?groupId=" + groupId + "&subjectId=" + subjectId;
    }

    @PostMapping("/{id}/delete")
    public String deleteGrade(@PathVariable Long id) {
        Grade existing = gradeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Оценка не найдена"));

        Long groupId = existing.getLesson().getGroup().getId();
        Long subjectId = existing.getLesson().getSubject().getId();

        gradeService.deleteById(id);

        return "redirect:/journal?groupId=" + groupId + "&subjectId=" + subjectId;
    }
}
