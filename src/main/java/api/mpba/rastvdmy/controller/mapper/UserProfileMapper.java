package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for UserRequest and UserResponse
 */
@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "surname", source = "surname")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "countryOfOrigin", source = "countryOfOrigin")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "avatar", source = "avatar")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "role", source = "role")
    UserProfileRequest toRequest(UserProfileResponse userProfileResponse);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "surname", source = "surname")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "countryOfOrigin", source = "countryOfOrigin")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "avatar", source = "avatar")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "role", source = "role")
    UserProfileResponse toResponse(UserProfileRequest userProfileRequest);
}
