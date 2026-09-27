package com.berkay.todo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatusRequest {
    @NotNull(message = "Tamamlandı bilgisi boş bırakılamaz")
    private Boolean completed;


}
