package ru.coursework.artschool.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.coursework.artschool.service.ReportService;

import java.util.List;
@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String reports(Model model) {
        var overview = reportService.getOverviewStats();
        var groupStats = reportService.getGroupStudentStats();
        var subjectStats = reportService.getSubjectAverageStats();

        model.addAttribute("title", "Отчёты и статистика");
        model.addAttribute("overview", overview);
        model.addAttribute("groupStats", groupStats);
        model.addAttribute("subjectStats", subjectStats);

        // данные для графиков
        List<String> groupLabels = groupStats.stream()
                .map(s -> s.getGroup().getCode())
                .toList();

        List<Long> groupStudentCounts = groupStats.stream()
                .map(ru.coursework.artschool.service.ReportService.GroupStudentStats::getStudentCount)
                .toList();

        List<Double> groupAvgGrades = groupStats.stream()
                .map(s -> s.getAverageGrade() == null ? 0.0 : s.getAverageGrade())
                .toList();

        List<String> subjectLabels = subjectStats.stream()
                .map(s -> s.getSubject().getCode())
                .toList();

        List<Double> subjectAvgGrades = subjectStats.stream()
                .map(s -> s.getAverageGrade() == null ? 0.0 : s.getAverageGrade())
                .toList();

        model.addAttribute("groupLabels", groupLabels);
        model.addAttribute("groupStudentCounts", groupStudentCounts);
        model.addAttribute("groupAvgGrades", groupAvgGrades);
        model.addAttribute("subjectLabels", subjectLabels);
        model.addAttribute("subjectAvgGrades", subjectAvgGrades);

        return "reports";
    }
}
