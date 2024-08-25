package accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankIdentityRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.BankIdentityResponse;
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
    BankIdentityRequest toRequest(BankIdentityResponse bankIdentityResponse);

    /**
     * Maps BankIdentityRequest to BankIdentityResponse
     * @param bankIdentityRequest BankIdentityRequest
     * @return BankIdentityResponse
     */
    @Mapping(target = "bankName", source = "bankName")
    BankIdentityResponse toResponse(BankIdentityRequest bankIdentityRequest);
}
