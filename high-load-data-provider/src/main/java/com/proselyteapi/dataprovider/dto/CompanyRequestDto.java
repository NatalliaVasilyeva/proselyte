package com.proselyteapi.dataprovider.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;


@Builder(toBuilder = true)
@Data
public class CompanyRequestDto {
    private String name;

    @Pattern(regexp = "^\\w{1,5}$", flags = { Pattern.Flag.CASE_INSENSITIVE }, message = "The symbol is invalid.")
    @Size(min = 3, max = 5, message = "The length of symbol must be between 2 and 5 characters.")
    private String symbol;
    private boolean enabled;
}