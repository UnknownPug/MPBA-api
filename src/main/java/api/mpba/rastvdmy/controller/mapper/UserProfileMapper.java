package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between {@link UserProfileRequest} and {@link UserProfileResponse}.
 * <p>
 * This interface uses MapStruct to automatically generate the implementation for mapping fields
 * between the two data transfer objects (DTOs) related to user profiles.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    /**
     * Maps a {@link UserProfileResponse} object to a {@link UserProfileRequest} object.
     *
     * @param userProfileResponse The {@link UserProfileResponse} object to be converted.
     * @return The converted {@link UserProfileRequest} object.
     */
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

    /**
     * Maps a {@link UserProfileRequest} object to a {@link UserProfileResponse} object.
     *
     * @param userProfileRequest The {@link UserProfileRequest} object to be converted.
     * @return The converted {@link UserProfileResponse} object.
     */
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
