package com.proselyteapi.dataprovider.mapper;

import com.proselyteapi.dataprovider.dto.UserDto;
import com.proselyteapi.dataprovider.entity.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto map(User user);

    @InheritInverseConfiguration
    User map(UserDto dto);
}