package ru.coursework.artschool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Главная — художественная школа");
        model.addAttribute("message", "Здесь будут сегодняшние занятия и ближайшие выставки.");
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "Об авторе");
        model.addAttribute("fio", "ФИО Студента");
        model.addAttribute("group", "Группа XYZ");
        model.addAttribute("email", "email@example.com");
        return "about";
    }


    @GetMapping("/exhibitions")
    public String exhibitions(Model model) {
        model.addAttribute("title", "Выставки");
        return "exhibitions";
    }

    @GetMapping("/exhibitions/{id}")
    public String exhibitionDetails(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Карточка выставки #" + id);
        model.addAttribute("exhibitionId", id);
        return "exhibition-details";
    }

}
