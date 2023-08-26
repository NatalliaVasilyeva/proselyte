package com.proselyteapi.dataprovider.mapper;

import com.proselyteapi.dataprovider.dto.StockRequestDto;
import com.proselyteapi.dataprovider.dto.StockResponseDto;
import com.proselyteapi.dataprovider.entity.Stock;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    StockMapper MAPPER = Mappers.getMapper(StockMapper.class );

    StockResponseDto map(Stock stock);

    @InheritInverseConfiguration
    @Mapping(target="id", ignore = true)
    Stock map(StockRequestDto dto);

    List<StockResponseDto> mapStockList(List<Stock> stocks);

    @InheritInverseConfiguration
    List<Stock> mapStockDtoList(List<StockRequestDto> stockDtos);
}