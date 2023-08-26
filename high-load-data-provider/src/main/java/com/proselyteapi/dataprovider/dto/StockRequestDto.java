package com.proselyteapi.dataprovider.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockRequestDto {
    private Long id;

    @Pattern(regexp = "^\\w{1,5}$", flags = { Pattern.Flag.CASE_INSENSITIVE }, message = "The symbol is invalid.")
    @Size(min = 3, max = 5, message = "The length of symbol must be between 2 and 5 characters.")
    private String symbol;
    private Double price;
    private Long companyId;
}