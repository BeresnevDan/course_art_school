package ru.coursework.artschool.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.coursework.artschool.model.Student;
import ru.coursework.artschool.model.StudentForm;
import ru.coursework.artschool.service.GroupService;
import ru.coursework.artschool.service.StudentService;
import ru.coursework.artschool.service.GradeService;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final GroupService groupService;
    private final GradeService gradeService;

    public StudentController(StudentService studentService,
                             GroupService groupService,
                             GradeService gradeService) {
        this.studentService = studentService;
        this.groupService = groupService;
        this.gradeService = gradeService;
    }


    // Список учеников + поиск + фильтр по группе
    // Доступ: Teacher, Admin (это уже ограничено в SecurityConfig)
    @GetMapping
    public String listStudents(@RequestParam(name = "q", required = false) String query,
                               @RequestParam(name = "groupId", required = false) Long groupId,
                               Model model) {
        List<Student> students = studentService.search(query, groupId);

        model.addAttribute("title", "Ученики");
        model.addAttribute("students", students);
        model.addAttribute("query", query);
        model.addAttribute("groupId", groupId);
        model.addAttribute("groups", groupService.findAll());

        return "students";
    }

    // Карточка ученика — Teacher и Admin
    @GetMapping("/{id}")
    public String studentDetails(@PathVariable Long id, Model model) {
        Student student = studentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ученик не найден"));

        GradeService.StudentPerformance performance =
                gradeService.getStudentPerformance(student.getId());

        model.addAttribute("title", "Ученик: " + student.getFullName());
        model.addAttribute("student", student);
        model.addAttribute("performance", performance);

        return "student-details";
    }


    // Форма создания — только Admin
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newStudentForm(Model model) {
        model.addAttribute("title", "Новый ученик");
        model.addAttribute("studentForm", new StudentForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("groups", groupService.findAll());
        return "student-form";
    }

    // Обработка создания — только Admin
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String createStudent(@Valid @ModelAttribute("studentForm") StudentForm form,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Новый ученик");
            model.addAttribute("isEdit", false);
            model.addAttribute("groups", groupService.findAll());
            return "student-form";
        }

        studentService.createFromForm(form);
        return "redirect:/students";
    }

    // Форма редактирования — только Admin
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editStudentForm(@PathVariable Long id, Model model) {
        Student student = studentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ученик не найден"));

        StudentForm form = studentService.toForm(student);

        model.addAttribute("title", "Редактирование ученика");
        model.addAttribute("studentForm", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("groups", groupService.findAll());
        return "student-form";
    }

    // Обработка редактирования — только Admin
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String updateStudent(@PathVariable Long id,
                                @Valid @ModelAttribute("studentForm") StudentForm form,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Редактирование ученика");
            model.addAttribute("isEdit", true);
            model.addAttribute("groups", groupService.findAll());
            return "student-form";
        }

        studentService.updateFromForm(id, form);
        return "redirect:/students";
    }

    // Удаление — только Admin
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteById(id);
        return "redirect:/students";
    }
}
