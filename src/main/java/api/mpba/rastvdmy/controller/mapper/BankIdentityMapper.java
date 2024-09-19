package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.BankIdentityRequest;
import api.mpba.rastvdmy.dto.response.BankIdentityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for BankIdentityRequest and BankIdentityResponse
 */
@Mapper(componentModel = "spring")
public interface BankIdentityMapper {

    /**
     * Maps BankIdentityResponse to BankIdentityRequest
     * @param bankIdentityResponse BankIdentityResponse
     * @return BankIdentityRequest
     */
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "bankNumber", source = "bankNumber")
    @Mapping(target = "swift", source = "swift")
    BankIdentityRequest toRequest(BankIdentityResponse bankIdentityResponse);

    /**
     * Maps BankIdentityRequest to BankIdentityResponse
     * @param bankIdentityRequest BankIdentityRequest
     * @return BankIdentityResponse
     */
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "bankNumber", source = "bankNumber")
    @Mapping(target = "swift", source = "swift")
    BankIdentityResponse toResponse(BankIdentityRequest bankIdentityRequest);
}
