package com.proselyteapi.dataprovider.mapper;

import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.dto.CompanyResponseDto;
import com.proselyteapi.dataprovider.entity.Company;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyMapper MAPPER = Mappers.getMapper(CompanyMapper.class);

    @Mapping(target = "stockDtos", source = "stocks",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    CompanyResponseDto map(Company company);

    @Mapping(target = "id", ignore = true)
    @InheritInverseConfiguration
    Company map(CompanyRequestDto dto);

    List<CompanyResponseDto> mapCompanyList(List<Company> companies);

    List<Company> mapCompanyDtoList(List<CompanyRequestDto> companyRequestDtos);
}