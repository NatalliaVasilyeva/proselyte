package com.proselyteapi.dataprovider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String symbol;
    private boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<StockRequestDto> stockDtos;
}