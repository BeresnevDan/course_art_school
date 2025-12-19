package ru.coursework.artschool.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.coursework.artschool.model.Lesson;
import ru.coursework.artschool.model.LessonForm;
import ru.coursework.artschool.service.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final SubjectService subjectService;
    private final GroupService groupService;
    private final TeacherService teacherService;
    private final RoomService roomService;

    public LessonController(LessonService lessonService,
                            SubjectService subjectService,
                            GroupService groupService,
                            TeacherService teacherService,
                            RoomService roomService) {
        this.lessonService = lessonService;
        this.subjectService = subjectService;
        this.groupService = groupService;
        this.teacherService = teacherService;
        this.roomService = roomService;
    }

    // Просмотр расписания на день (доступен всем)
    @GetMapping
    public String listLessons(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {

        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();

        List<Lesson> lessons = lessonService.findByDate(effectiveDate);

        model.addAttribute("title", "Расписание занятий");
        model.addAttribute("lessons", lessons);
        model.addAttribute("date", effectiveDate);

        return "lessons";
    }

    // ======== СОЗДАНИЕ ЗАНЯТИЯ (ADMIN) ========

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newLessonForm(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {

        LessonForm form = new LessonForm();
        form.setDate(date != null ? date : LocalDate.now());

        model.addAttribute("title", "Новое занятие");
        model.addAttribute("lessonForm", form);
        model.addAttribute("isEdit", false);
        fillReferenceData(model);

        return "lesson-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String createLesson(@Valid @ModelAttribute("lessonForm") LessonForm form,
                               BindingResult bindingResult,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Новое занятие");
            model.addAttribute("isEdit", false);
            fillReferenceData(model);
            return "lesson-form";
        }

        try {
            Lesson lesson = new Lesson();
            lesson.setDate(form.getDate());
            lesson.setStartTime(form.getStartTime());
            lesson.setEndTime(form.getEndTime());
            lesson.setType(form.getType());
            lesson.setNotes(form.getNotes());

            lessonService.create(
                    lesson,
                    form.getSubjectId(),
                    form.getGroupId(),
                    form.getTeacherId(),
                    form.getRoomId()
            );
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("lesson.error", ex.getMessage());
            model.addAttribute("title", "Новое занятие");
            model.addAttribute("isEdit", false);
            fillReferenceData(model);
            return "lesson-form";
        }

        // после создания возвращаемся на расписание на дату этого занятия
        return "redirect:/lessons?date=" + form.getDate();
    }

    // ======== РЕДАКТИРОВАНИЕ ЗАНЯТИЯ (ADMIN) ========

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editLessonForm(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Занятие не найдено"));

        LessonForm form = lessonService.toForm(lesson);

        model.addAttribute("title", "Редактирование занятия");
        model.addAttribute("lessonForm", form);
        model.addAttribute("isEdit", true);
        fillReferenceData(model);

        return "lesson-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String updateLesson(@PathVariable Long id,
                               @Valid @ModelAttribute("lessonForm") LessonForm form,
                               BindingResult bindingResult,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Редактирование занятия");
            model.addAttribute("isEdit", true);
            fillReferenceData(model);
            return "lesson-form";
        }

        try {
            Lesson tmp = new Lesson();
            tmp.setDate(form.getDate());
            tmp.setStartTime(form.getStartTime());
            tmp.setEndTime(form.getEndTime());
            tmp.setType(form.getType());
            tmp.setNotes(form.getNotes());

            lessonService.update(
                    id,
                    tmp,
                    form.getSubjectId(),
                    form.getGroupId(),
                    form.getTeacherId(),
                    form.getRoomId()
            );
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("lesson.error", ex.getMessage());
            model.addAttribute("title", "Редактирование занятия");
            model.addAttribute("isEdit", true);
            fillReferenceData(model);
            return "lesson-form";
        }

        return "redirect:/lessons?date=" + form.getDate();
    }

    // ======== УДАЛЕНИЕ ЗАНЯТИЯ (ADMIN) ========

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteLesson(@PathVariable Long id,
                               @RequestParam(name = "date", required = false)
                               @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        lessonService.deleteById(id);

        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/lessons?date=" + redirectDate;
    }

    private void fillReferenceData(Model model) {
        model.addAttribute("subjects", subjectService.findAll());
        model.addAttribute("groups", groupService.findAll());
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("rooms", roomService.findAll());

        // фиксированные стартовые времена (можно потом вынести в конфиг)
        model.addAttribute("defaultStartTimes", List.of(
                LocalTime.of(9, 0),
                LocalTime.of(10, 40),
                LocalTime.of(14, 0),
                LocalTime.of(15, 40)
        ));
    }
}
