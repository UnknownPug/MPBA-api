package accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.CardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for CardRequest and CardResponse
 */
@Mapper(componentModel = "spring")
public interface CardMapper {

    /**
     * Maps CardResponse to CardRequest
     * @param cardResponse CardResponse
     * @return CardRequest
     */
    @Mapping(target = "cardNumber", source = "cardNumber")
    @Mapping(target = "cvv", source = "cvv")
    @Mapping(target = "pin", source = "pin")
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    CardRequest toRequest(CardResponse cardResponse);

    /**
     * Maps CardRequest to CardResponse
     * @param cardRequest CardRequest
     * @return CardResponse
     */
    @Mapping(target = "cardNumber", source = "cardNumber")
    @Mapping(target = "cvv", source = "cvv")
    @Mapping(target = "pin", source = "pin")
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    CardResponse toResponse(CardRequest cardRequest);
}
