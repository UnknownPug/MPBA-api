package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.UserRequest;
import api.mpba.rastvdmy.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for UserRequest and UserResponse
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps UserResponse to UserRequest
     * @param userResponse UserResponse
     * @return UserRequest
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "surname", source = "surname")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "countryOfOrigin", source = "countryOfOrigin")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    UserRequest toRequest(UserResponse userResponse);

    /**
     * Maps UserRequest to UserResponse
     * @param userRequest UserRequest
     * @return UserResponse
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "surname", source = "surname")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "countryOfOrigin", source = "countryOfOrigin")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    UserResponse toResponse(UserRequest userRequest);
}
