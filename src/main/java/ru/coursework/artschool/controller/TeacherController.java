package ru.coursework.artschool.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.coursework.artschool.model.Teacher;
import ru.coursework.artschool.service.TeacherService;

import java.util.List;

import ru.coursework.artschool.model.TeacherForm;
import ru.coursework.artschool.service.SubjectService;


@Controller
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final SubjectService subjectService;

    public TeacherController(TeacherService teacherService,
                             SubjectService subjectService) {
        this.teacherService = teacherService;
        this.subjectService = subjectService;
    }


    // Список преподавателей + поиск (доступен всем)
    @GetMapping
    public String listTeachers(@RequestParam(name = "q", required = false) String query,
                               Model model) {
        List<Teacher> teachers = teacherService.search(query);
        model.addAttribute("title", "Преподаватели");
        model.addAttribute("teachers", teachers);
        model.addAttribute("query", query);
        return "teachers";
    }

    // Карточка преподавателя (доступна всем)
    @GetMapping("/{id}")
    public String teacherDetails(@PathVariable Long id, Model model) {
        Teacher teacher = teacherService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));

        model.addAttribute("title", "Преподаватель: " + teacher.getFullName());
        model.addAttribute("teacher", teacher);
        return "teacher-details";
    }

    // Форма создания (только ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newTeacherForm(Model model) {
        model.addAttribute("title", "Новый преподаватель");
        model.addAttribute("teacherForm", new TeacherForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("subjects", subjectService.findAll());
        return "teacher-form";
    }

    // Обработка создания (только ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String createTeacher(@Valid @ModelAttribute("teacherForm") TeacherForm teacherForm,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Новый преподаватель");
            model.addAttribute("isEdit", false);
            model.addAttribute("subjects", subjectService.findAll());
            return "teacher-form";
        }

        teacherService.createFromForm(teacherForm);
        return "redirect:/teachers";
    }

    // Форма редактирования (только ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editTeacherForm(@PathVariable Long id, Model model) {
        Teacher teacher = teacherService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));

        TeacherForm form = teacherService.toForm(teacher);

        model.addAttribute("title", "Редактирование преподавателя");
        model.addAttribute("teacherForm", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("subjects", subjectService.findAll());
        return "teacher-form";
    }

    // Обработка редактирования (только ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String updateTeacher(@PathVariable Long id,
                                @Valid @ModelAttribute("teacherForm") TeacherForm teacherForm,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Редактирование преподавателя");
            model.addAttribute("isEdit", true);
            model.addAttribute("subjects", subjectService.findAll());
            return "teacher-form";
        }

        teacherService.updateFromForm(id, teacherForm);
        return "redirect:/teachers";
    }


    // Удаление (только ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteTeacher(@PathVariable Long id) {
        teacherService.deleteById(id);
        return "redirect:/teachers";
    }
}
