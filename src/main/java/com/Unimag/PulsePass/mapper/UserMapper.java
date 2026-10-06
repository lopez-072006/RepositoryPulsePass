package com.Unimag.PulsePass.mapper;

import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "firstName", source = "userProfile.firstName")
    @Mapping(target = "lastName", source = "userProfile.lastName")
    @Mapping(target = "phone", source = "userProfile.phone")
    @Mapping(target = "city", source = "userProfile.city")
    @Mapping(target = "birthDate", source = "userProfile.birthDate")
    UserResponse toResponse(User user);
}
