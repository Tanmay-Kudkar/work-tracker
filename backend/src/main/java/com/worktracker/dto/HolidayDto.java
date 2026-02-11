package com.worktracker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDto {
    private String name;
    private LocalDate date;
    private String description;
}
