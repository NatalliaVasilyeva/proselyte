package com.proselyteapi.stocksreceiver.mapper;

import com.proselyteapi.stocksreceiver.dto.StockDto;
import com.proselyteapi.stocksreceiver.entity.Stock;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StockMapper {

    StockMapper MAPPER = Mappers.getMapper(StockMapper.class );

    StockDto map(Stock stock);

    @InheritInverseConfiguration
    @Mapping(target="id", ignore = true)
    Stock map(StockDto dto);

}