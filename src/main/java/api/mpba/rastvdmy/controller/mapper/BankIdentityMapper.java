package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.BankIdentityRequest;
import api.mpba.rastvdmy.dto.response.BankIdentityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between {@link BankIdentityRequest} and {@link BankIdentityResponse}.
 * <p>
 * This interface uses MapStruct to generate the implementation for mapping fields
 * between the two data transfer objects (DTOs) related to bank identities.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface BankIdentityMapper {

    /**
     * Maps a {@link BankIdentityResponse} object to a {@link BankIdentityRequest} object.
     *
     * @param bankIdentityResponse The {@link BankIdentityResponse} object to be converted.
     * @return The converted {@link BankIdentityRequest} object.
     */
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "bankNumber", source = "bankNumber")
    @Mapping(target = "swift", source = "swift")
    BankIdentityRequest toRequest(BankIdentityResponse bankIdentityResponse);

    /**
     * Maps a {@link BankIdentityRequest} object to a {@link BankIdentityResponse} object.
     *
     * @param bankIdentityRequest The {@link BankIdentityRequest} object to be converted.
     * @return The converted {@link BankIdentityResponse} object.
     */
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "bankNumber", source = "bankNumber")
    @Mapping(target = "swift", source = "swift")
    BankIdentityResponse toResponse(BankIdentityRequest bankIdentityRequest);
}
