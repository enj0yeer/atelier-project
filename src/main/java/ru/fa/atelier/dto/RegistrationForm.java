package ru.fa.atelier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationForm {
    @NotBlank(message = "Укажите ФИО")
    @Size(max = 160, message = "ФИО не должно превышать 160 символов")
    private String fullName;

    @NotBlank(message = "Укажите электронную почту")
    @Email(message = "Введите корректный адрес электронной почты")
    @Size(max = 160, message = "Email не должен превышать 160 символов")
    private String email;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 8, max = 72, message = "Пароль должен содержать от 8 до 72 символов")
    private String password;

    @Size(max = 30, message = "Телефон не должен превышать 30 символов")
    private String phone;
}
