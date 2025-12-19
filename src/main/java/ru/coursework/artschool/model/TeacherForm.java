package ru.coursework.artschool.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class TeacherForm {

    private Long id;

    @NotBlank(message = "ФИО обязательно")
    @Size(max = 255, message = "Слишком длинное ФИО")
    private String fullName;

    @NotBlank(message = "Специализация обязательна")
    @Size(max = 255, message = "Слишком длинная специализация")
    private String specialization;

    @Email(message = "Некорректный email")
    @Size(max = 255, message = "Слишком длинный email")
    private String email;

    @Size(max = 64, message = "Слишком длинный телефон")
    private String phone;

    private Integer experienceYears;

    @Size(max = 2000, message = "Описание слишком длинное")
    private String bio;

    // список id дисциплин, которые ведёт преподаватель
    private List<Long> subjectIds = new ArrayList<>();

    public TeacherForm() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName != null ? fullName.trim() : null;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization != null ? specialization.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio != null ? bio.trim() : null;
    }

    public List<Long> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<Long> subjectIds) {
        this.subjectIds = subjectIds;
    }
}
