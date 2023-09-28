package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.dto.CompanyResponseDto;
import com.proselyteapi.dataprovider.entity.Company;
import com.proselyteapi.dataprovider.entity.Stock;
import com.proselyteapi.dataprovider.mapper.CompanyMapper;
import com.proselyteapi.dataprovider.repository.CompanyRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final ReactiveRedisTemplate<String, Company> reactiveRedisCompanyTemplate;
    private final ReactiveRedisTemplate<String, Stock> reactiveRedisStockTemplate;
    private final CompanyRepository companyRepository;
    private final StockRepository stockRepository;
    public static final String COMPANY_KEY = "company:";
    public static final String STOCK_KEY = "stock:";

    public Mono<CompanyResponseDto> saveCompany(CompanyRequestDto companyRequestDto) {
        return Mono.just(companyRequestDto)
            .map(CompanyMapper.MAPPER::map)
            .flatMap(company -> companyRepository.save(company)
                .flatMap(savedCompany -> reactiveRedisCompanyTemplate.opsForValue().set(COMPANY_KEY + savedCompany.getSymbol(), savedCompany).thenReturn(savedCompany)))
            .map(CompanyMapper.MAPPER::map);
    }

    public Flux<CompanyResponseDto> saveCompanies(List<CompanyRequestDto> companyRequestDtos) {
        return Flux.fromIterable(companyRequestDtos)
            .switchIfEmpty(Flux.empty())
            .map(CompanyMapper.MAPPER::map)
            .flatMap(companies -> companyRepository.save(companies)
                .flatMap(savedCompany -> reactiveRedisCompanyTemplate.opsForValue().set(COMPANY_KEY + savedCompany.getSymbol(), savedCompany).thenReturn(savedCompany)))
            .collectList()
            .map(CompanyMapper.MAPPER::mapCompanyList)
            .flatMapMany(Flux::fromIterable);
    }

    public Flux<CompanyResponseDto> getAllCompaniesWithStocks() {
        return reactiveRedisCompanyTemplate.keys(COMPANY_KEY + "*")
            // Fetching cached companies.
            .flatMap(key -> reactiveRedisCompanyTemplate.opsForValue().get(key))
            // If cache is empty, fetch the database for companies
            .switchIfEmpty(companyRepository.findAll()
                // Persisting the fetched companies in the cache.
                .flatMap(company -> {
                        stockRepository.findAllBySymbol(company.getSymbol()).collectList()
                            .map(stocks -> {
                                company.setStocks(stocks);
                                return stocks;
                            });
                        return reactiveRedisCompanyTemplate
                            .opsForValue()
                            .set(COMPANY_KEY + company.getSymbol(), company);
                    }
                )
                // Fetching the companies from the updated cache.
                .thenMany(reactiveRedisCompanyTemplate
                    .keys(COMPANY_KEY + "*")
                    .flatMap(key -> reactiveRedisCompanyTemplate.opsForValue().get(key))
                )
            )
            .collectList()
            .flatMapIterable(CompanyMapper.MAPPER::mapCompanyList);
    }

    public Flux<CompanyResponseDto> getAllCompaniesWithoutStocks() {
        return reactiveRedisCompanyTemplate.keys(COMPANY_KEY + "*")
            // Fetching cached companies.
            .flatMap(key -> reactiveRedisCompanyTemplate.opsForValue().get(key))
            // If cache is empty, fetch the database for companies
            .switchIfEmpty(Flux.defer(() -> companyRepository.findAll()
                // Persisting the fetched companies in the cache.
                .flatMap(company -> reactiveRedisCompanyTemplate
                        .opsForValue()
                        .set(COMPANY_KEY + company.getSymbol(), company)
                    .thenReturn(company)
                ))
            )
            .collectList()
            .flatMapIterable(CompanyMapper.MAPPER::mapCompanyList);
    }

    public Mono<CompanyResponseDto> getBySymbol(String symbol) {
        return reactiveRedisCompanyTemplate.opsForValue().get(COMPANY_KEY + symbol)
            .switchIfEmpty(Mono.defer(() -> Mono.zip(companyRepository.findBySymbol(symbol), stockRepository.findAllBySymbol(symbol).collectList())
                .map(tuples -> {
                    var company = tuples.getT1();
                    var stocks = tuples.getT2();
                    company.setStocks(stocks);
                    return company;
                })
                .cast(Company.class)
                .flatMap(company -> reactiveRedisCompanyTemplate.opsForValue().set(COMPANY_KEY + company.getSymbol(), company)
                    .thenReturn(company))
            ))
            .map(CompanyMapper.MAPPER::map);
    }

    public void updateCompany(Long id, CompanyRequestDto companyRequestDto) {
        companyRepository.findById(id)
            .switchIfEmpty(Mono.error(new NoSuchElementException(String.format("Company with id %s does not exist", id))))
            .flatMap(dbCompany -> {
                dbCompany.setName(companyRequestDto.getName());
                dbCompany.setSymbol(companyRequestDto.getSymbol());
                dbCompany.setEnabled(companyRequestDto.isEnabled());
                return companyRepository.save(dbCompany);
            })
            .flatMap(company -> reactiveRedisCompanyTemplate.opsForValue().delete(COMPANY_KEY + company.getSymbol()))
            .then();
    }

    public Mono<Void> deleteById(Long companyId) {
       return companyRepository.findById(companyId)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new NoSuchElementException(String.format("Company with id %s does not exist", companyId)))))
            .flatMap(company -> stockRepository.findAllBySymbol(company.getSymbol())
                .flatMap(stock ->
                    stockRepository.delete(stock)
                        .then(reactiveRedisStockTemplate.delete(STOCK_KEY + stock.getId())))
                .then(companyRepository.delete(company))
                .then(reactiveRedisCompanyTemplate.delete(COMPANY_KEY + company.getId())))
            .then();
    }

    public Mono<Void> deleteAll() {
        return companyRepository.findAll()
            .switchIfEmpty(Mono.defer(() -> Mono.error(new NoSuchElementException("No companies exists"))))
            .flatMap(company -> stockRepository.findAllBySymbol(company.getSymbol())
                .flatMap(stock ->
                    stockRepository.delete(stock)
                        .then(reactiveRedisStockTemplate.delete(STOCK_KEY + stock.getId())))
                .then(companyRepository.delete(company))
                .then(reactiveRedisCompanyTemplate.delete(COMPANY_KEY + company.getId())))
            .then();
    }
}