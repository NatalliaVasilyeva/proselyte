package com.proselyteapi.stocksreceiver.mapper;

import com.proselyteapi.stocksreceiver.dto.CompanyDto;
import com.proselyteapi.stocksreceiver.entity.Company;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyMapper MAPPER = Mappers.getMapper(CompanyMapper.class);

    @Mapping(target = "isEnabled", source = "enabled")
    CompanyDto map(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", source = "isEnabled")
    @InheritInverseConfiguration
    Company map(CompanyDto dto);

}