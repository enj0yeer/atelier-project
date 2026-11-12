package ru.fa.atelier.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServiceForm {
    @NotBlank(message = "Укажите название услуги")
    @Size(max = 120)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Укажите стоимость")
    @DecimalMin(value = "0.01", message = "Стоимость должна быть больше нуля")
    @Digits(integer = 10, fraction = 2, message = "Стоимость должна содержать не более 10 цифр до запятой и 2 после")
    private BigDecimal price;
}
