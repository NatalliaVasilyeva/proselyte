package com.proselyteapi.dataprovider.controller;

import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.dto.CompanyResponseDto;
import com.proselyteapi.dataprovider.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "Company API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Validated
public class CompanyRestController {

    private final CompanyService companyService;

    public static final String API_KEY_HEADER = "x-api-key";

    @GetMapping("/companies")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get information about all companies")
    public Flux<CompanyResponseDto> getAllCompanies(@RequestHeader(name = API_KEY_HEADER) @Valid String apiKey) {
        return companyService.getAllCompaniesWithoutStocks();
    }

    @PostMapping("/company")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Create company")
    public Mono<CompanyResponseDto> createCompany(@RequestHeader(name = API_KEY_HEADER) @Valid String apiKey,
                                                  @Parameter(required = true) @Valid @RequestBody CompanyRequestDto companyRequestDto) {
        return companyService.saveCompany(companyRequestDto);
    }

    @PostMapping("/companies")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Create company")
    public Flux<CompanyResponseDto> createCompanies(@RequestHeader(name = API_KEY_HEADER) @Valid String apiKey,
                                                  @Parameter(required = true) @Valid @RequestBody List<CompanyRequestDto> companyRequestDto) {
        return companyService.saveCompanies(companyRequestDto);
    }

    @GetMapping("/companies/{symbol}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Create company")
    public Mono<CompanyResponseDto> getCompanyBySymbol(@RequestHeader(name = API_KEY_HEADER) @Valid String apiKey,
                                                    @Parameter(required = true, name = "symbol") @Valid @PathVariable String symbol) {
        return companyService.getBySymbol(symbol);
    }
}