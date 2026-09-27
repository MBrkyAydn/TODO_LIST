package com.berkay.todo.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Başlık boş bırakılamaz")
    private String title;


    @NotBlank(message = "Açıklama boş bırakılamaz")
    private String description;

    @NotNull(message = "Tamamlandı bilgisi boş bırakılamaz")
    private Boolean completed;


}
