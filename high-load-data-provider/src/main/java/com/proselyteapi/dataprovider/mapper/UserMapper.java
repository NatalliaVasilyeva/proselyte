package com.proselyteapi.dataprovider.mapper;

import com.proselyteapi.dataprovider.dto.UserDto;
import com.proselyteapi.dataprovider.entity.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper MAPPER = Mappers.getMapper(UserMapper.class );

    UserDto map(User user);

    @InheritInverseConfiguration
    User map(UserDto dto);
}