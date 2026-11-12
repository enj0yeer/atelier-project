package ru.fa.atelier.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException exception, Model model) {
        model.addAttribute("errorTitle", "Данные не найдены");
        model.addAttribute("errorMessage", "Запрошенная запись отсутствует или больше недоступна.");
        return "error/general";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException exception, Model model) {
        model.addAttribute("errorTitle", "Некорректный запрос");
        model.addAttribute("errorMessage", exception.getMessage());
        return "error/general";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(DataIntegrityViolationException exception, Model model) {
        model.addAttribute("errorTitle", "Конфликт данных");
        model.addAttribute("errorMessage", "Операция конфликтует с уже существующими данными.");
        return "error/general";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException exception, Model model) {
        model.addAttribute("errorTitle", "Доступ запрещён");
        model.addAttribute("errorMessage", "Недостаточно прав для выполнения операции.");
        return "error/general";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Exception exception, Model model) {
        model.addAttribute("errorTitle", "Не удалось выполнить операцию");
        model.addAttribute("errorMessage", "Произошла внутренняя ошибка. Повторите действие позднее.");
        return "error/general";
    }
}
