package ru.coursework.artschool.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupForm {

    private Long id;

    @NotBlank(message = "Код группы обязателен")
    @Size(max = 32, message = "Слишком длинный код группы")
    private String code;

    @NotBlank(message = "Название группы обязательно")
    @Size(max = 255, message = "Слишком длинное название")
    private String name;

    private Integer admissionYear;

    @Size(max = 64, message = "Слишком длинное значение уровня")
    private String level;

    @Size(max = 2000, message = "Описание слишком длинное")
    private String description;

    // здесь просто id преподавателя-куратора
    private Long curatorId;

    public GroupForm() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.trim() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public Integer getAdmissionYear() {
        return admissionYear;
    }

    public void setAdmissionYear(Integer admissionYear) {
        this.admissionYear = admissionYear;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level != null ? level.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public Long getCuratorId() {
        return curatorId;
    }

    public void setCuratorId(Long curatorId) {
        this.curatorId = curatorId;
    }
}
