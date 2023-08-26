package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.dto.CompanyResponseDto;
import com.proselyteapi.dataprovider.entity.Company;
import com.proselyteapi.dataprovider.entity.Stock;
import com.proselyteapi.dataprovider.mapper.CompanyMapper;
import com.proselyteapi.dataprovider.mapper.StockMapper;
import com.proselyteapi.dataprovider.repository.CompanyRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "companyCache")
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final StockRepository stockRepository;

    @CachePut(cacheNames = "company", key = "#symbol", unless = "#symbol==null")
    public Mono<CompanyResponseDto> createCompany(CompanyRequestDto companyRequestDto) {
        return Mono.just(companyRequestDto)
            .map(CompanyMapper.MAPPER::map)
            .flatMap(companyRepository::save)
            .map(CompanyMapper.MAPPER::map);
    }

    @CachePut(cacheNames = "company", key = "#symbol", unless = "#symbol==null")
    public Flux<CompanyResponseDto> createCompanies(List<CompanyRequestDto> companyRequestDtos) {
        return Flux.fromIterable(companyRequestDtos)
            .switchIfEmpty(Flux.empty())
            .flatMap(companyRequestDto -> companyRepository.save(CompanyMapper.MAPPER.map(companyRequestDto)))
            .collectList()
            .map(CompanyMapper.MAPPER::mapCompanyList)
            .flatMapMany(Flux::fromIterable);
    }

    @Cacheable(cacheNames = "companies")
    public Flux<CompanyResponseDto> getAllCompaniesWithStocks() {
        return companyRepository.findAll()
            .switchIfEmpty(Flux.empty())
            .flatMap(company -> stockRepository.findAllBySymbol(company.getSymbol()).collectList()
                .map(stocks -> {
                    company.setStocks(stocks);
                    return company;
                }))
            .collectList()
            .flatMapIterable(CompanyMapper.MAPPER::mapCompanyList);
    }

    @Cacheable(cacheNames = "companies")
    public Flux<CompanyResponseDto> getAllCompaniesWithoutStocks() {
        return companyRepository.findAll()
            .switchIfEmpty(Flux.empty())
            .collectList()
            .flatMapIterable(CompanyMapper.MAPPER::mapCompanyList);
    }

    @Cacheable(cacheNames = "company", key = "#symbol", unless = "#symbol == null")
    public Mono<CompanyResponseDto> getBySymbol(String symbol) {
        return Mono.zip(companyRepository.findBySymbol(symbol), stockRepository.findAllBySymbol(symbol).collectList())
            .map(tuples -> {
                var company = tuples.getT1();
                var stocks = tuples.getT2();
                company.setStocks(stocks);
                return company;
            })
            .map(CompanyMapper.MAPPER::map);
    }

    @CacheEvict(cacheNames = "company", key = "#id")
    public void updateCompany(Long id, CompanyRequestDto companyRequestDto) {
        companyRepository.findById(id)
            .switchIfEmpty(Mono.error(new NoSuchElementException(String.format("Company with id %s does not exist", id))))
            .flatMap(dbCompany -> {
                dbCompany.setName(companyRequestDto.getName());
                dbCompany.setSymbol(companyRequestDto.getSymbol());
                dbCompany.setEnabled(companyRequestDto.isEnabled());
                return companyRepository.save(dbCompany);
            });
    }

    @CacheEvict(cacheNames = "company", key = "#id")
    public void deleteById(Long companyId) {
        companyRepository.findById(companyId)
            .switchIfEmpty(Mono.error(new NoSuchElementException(String.format("Company with id %s does not exist", companyId))))
            .flatMap(company -> stockRepository.findAllBySymbol(company.getSymbol())
                .flatMap(stockRepository::delete)
                .then(companyRepository.delete(company)));
    }

    /* Clears cache after 10 minutes. */
    @CacheEvict(allEntries = true, cacheNames = { "company" })
    @Scheduled(fixedDelay = 600000)
    public void cacheEvict() {
        log.info("Cleaning cache company");
    }
}