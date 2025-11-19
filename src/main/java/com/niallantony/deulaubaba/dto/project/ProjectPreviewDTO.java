package com.niallantony.deulaubaba.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectPreviewDTO {
    private Long id;
    private boolean completed;
    private String description;
    private String objective;
    private String imgsrc;
    private LocalDate startedOn;
    private LocalDate completedOn;
}
