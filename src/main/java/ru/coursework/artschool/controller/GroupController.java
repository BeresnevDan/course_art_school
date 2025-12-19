package ru.coursework.artschool.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.coursework.artschool.model.Group;
import ru.coursework.artschool.model.GroupForm;
import ru.coursework.artschool.service.GroupService;
import ru.coursework.artschool.service.StudentService;
import ru.coursework.artschool.service.TeacherService;

import ru.coursework.artschool.model.Student;
import java.util.List;


import java.util.List;

@Controller
@RequestMapping("/groups")
@PreAuthorize("hasRole('ADMIN')") // весь контроллер только для ADMIN
public class GroupController {

    private final GroupService groupService;
    private final TeacherService teacherService;
    private final StudentService studentService;

    public GroupController(GroupService groupService,
                           TeacherService teacherService,
                           StudentService studentService) {
        this.groupService = groupService;
        this.teacherService = teacherService;
        this.studentService = studentService;
    }


    // список групп + поиск
    @GetMapping
    public String listGroups(@RequestParam(name = "q", required = false) String query,
                             Model model) {
        List<Group> groups = groupService.search(query);
        model.addAttribute("title", "Группы");
        model.addAttribute("groups", groups);
        model.addAttribute("query", query);
        return "groups";
    }

    // карточка группы
    @GetMapping("/{id}")
    public String groupDetails(@PathVariable Long id, Model model) {
        Group group = groupService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

        // Загружаем учеников этой группы
        List<Student> students = studentService.search(null, id);

        model.addAttribute("title", "Группа: " + group.getName());
        model.addAttribute("group", group);
        model.addAttribute("students", students);
        return "group-details";
    }


    // форма создания
    @GetMapping("/new")
    public String newGroupForm(Model model) {
        model.addAttribute("title", "Новая группа");
        model.addAttribute("groupForm", new GroupForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("teachers", teacherService.findAll());
        return "group-form";
    }

    // обработка создания
    @PostMapping
    public String createGroup(@Valid @ModelAttribute("groupForm") GroupForm form,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Новая группа");
            model.addAttribute("isEdit", false);
            model.addAttribute("teachers", teacherService.findAll());
            return "group-form";
        }

        try {
            groupService.createFromForm(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "code.exists", ex.getMessage());
            model.addAttribute("title", "Новая группа");
            model.addAttribute("isEdit", false);
            model.addAttribute("teachers", teacherService.findAll());
            return "group-form";
        }

        return "redirect:/groups";
    }

    // форма редактирования
    @GetMapping("/{id}/edit")
    public String editGroupForm(@PathVariable Long id, Model model) {
        Group group = groupService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

        GroupForm form = groupService.toForm(group);

        model.addAttribute("title", "Редактирование группы");
        model.addAttribute("groupForm", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("teachers", teacherService.findAll());
        return "group-form";
    }

    // обработка редактирования
    @PostMapping("/{id}")
    public String updateGroup(@PathVariable Long id,
                              @Valid @ModelAttribute("groupForm") GroupForm form,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Редактирование группы");
            model.addAttribute("isEdit", true);
            model.addAttribute("teachers", teacherService.findAll());
            return "group-form";
        }

        try {
            groupService.updateFromForm(id, form);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "code.exists", ex.getMessage());
            model.addAttribute("title", "Редактирование группы");
            model.addAttribute("isEdit", true);
            model.addAttribute("teachers", teacherService.findAll());
            return "group-form";
        }

        return "redirect:/groups";
    }

    // удаление
    @PostMapping("/{id}/delete")
    public String deleteGroup(@PathVariable Long id) {
        groupService.deleteById(id);
        return "redirect:/groups";
    }
}
