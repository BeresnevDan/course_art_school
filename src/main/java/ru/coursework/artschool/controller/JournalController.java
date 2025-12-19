package ru.coursework.artschool.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.coursework.artschool.model.*;
import ru.coursework.artschool.service.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/journal")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class JournalController {

    private final GroupService groupService;
    private final SubjectService subjectService;
    private final StudentService studentService;
    private final GradeService gradeService;

    public JournalController(GroupService groupService,
                             SubjectService subjectService,
                             StudentService studentService,
                             GradeService gradeService) {
        this.groupService = groupService;
        this.subjectService = subjectService;
        this.studentService = studentService;
        this.gradeService = gradeService;
    }

    @GetMapping
    public String viewJournal(
            @RequestParam(name = "groupId", required = false) Long groupId,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            Model model) {

        model.addAttribute("title", "Журнал успеваемости");
        model.addAttribute("groups", groupService.findAll());
        model.addAttribute("subjects", subjectService.findAll());
        model.addAttribute("selectedGroupId", groupId);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        if (groupId == null || subjectId == null) {
            // просто показываем форму выбора
            return "journal";
        }

        Group group = groupService.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

        Subject subject = subjectService.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Дисциплина не найдена"));

        // все студенты группы
        List<Student> students = studentService.search(null, groupId);

        GradeService.JournalData data =
                gradeService.getJournalDataForGroupAndSubject(group, subject, fromDate, toDate, students);

        model.addAttribute("group", group);
        model.addAttribute("subject", subject);
        model.addAttribute("journalLessons", data.getLessons());
        model.addAttribute("journalStudents", data.getStudents());
        model.addAttribute("journalData", data);

        return "journal";
    }
}
