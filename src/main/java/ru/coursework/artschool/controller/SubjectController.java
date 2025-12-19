package ru.coursework.artschool.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.coursework.artschool.model.Subject;
import ru.coursework.artschool.service.SubjectService;

import java.util.List;

@Controller
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    // Список дисциплин + поиск
    @GetMapping
    public String listSubjects(@RequestParam(name = "q", required = false) String query,
                               Model model) {
        List<Subject> subjects = subjectService.search(query);
        model.addAttribute("title", "Дисциплины");
        model.addAttribute("subjects", subjects);
        model.addAttribute("query", query);
        return "subjects";
    }

    // Форма создания новой дисциплины
    @GetMapping("/new")
    public String newSubjectForm(Model model) {
        model.addAttribute("title", "Новая дисциплина");
        model.addAttribute("subject", new Subject());
        model.addAttribute("isEdit", false);
        return "subject-form";
    }

    // Обработка создания
    @PostMapping
    public String createSubject(@Valid @ModelAttribute("subject") Subject subject,
                                BindingResult bindingResult,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Новая дисциплина");
            model.addAttribute("isEdit", false);
            return "subject-form";
        }

        try {
            subjectService.create(subject);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "code.exists", ex.getMessage());
            model.addAttribute("title", "Новая дисциплина");
            model.addAttribute("isEdit", false);
            return "subject-form";
        }

        return "redirect:/subjects";
    }

    // Форма редактирования
    @GetMapping("/{id}/edit")
    public String editSubjectForm(@PathVariable Long id, Model model) {
        Subject subject = subjectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Дисциплина не найдена"));

        model.addAttribute("title", "Редактирование дисциплины");
        model.addAttribute("subject", subject);
        model.addAttribute("isEdit", true);
        return "subject-form";
    }

    // Обработка редактирования
    @PostMapping("/{id}")
    public String updateSubject(@PathVariable Long id,
                                @Valid @ModelAttribute("subject") Subject subject,
                                BindingResult bindingResult,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Редактирование дисциплины");
            model.addAttribute("isEdit", true);
            return "subject-form";
        }

        try {
            subjectService.update(id, subject);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "code.exists", ex.getMessage());
            model.addAttribute("title", "Редактирование дисциплины");
            model.addAttribute("isEdit", true);
            return "subject-form";
        }

        return "redirect:/subjects";
    }

    // Удаление
    @PostMapping("/{id}/delete")
    public String deleteSubject(@PathVariable Long id) {
        subjectService.deleteById(id);
        return "redirect:/subjects";
    }
}
