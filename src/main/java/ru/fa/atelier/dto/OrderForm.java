package ru.fa.atelier.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OrderForm {
    @NotNull(message = "Выберите услугу")
    private Long serviceId;

    @NotNull(message = "Укажите количество")
    @Min(value = 1, message = "Количество должно быть не меньше 1")
    @Max(value = 100, message = "Количество не должно превышать 100")
    private Integer quantity = 1;

    @NotNull(message = "Укажите плановую дату готовности")
    @FutureOrPresent(message = "Дата готовности не может быть в прошлом")
    private LocalDate dueDate;

    @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
    private String note;
}
