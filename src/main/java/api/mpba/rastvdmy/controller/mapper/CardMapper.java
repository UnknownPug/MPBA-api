package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.CardRequest;
import api.mpba.rastvdmy.dto.response.CardResponse;
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
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "expirationDate", source = "expirationDate")
    @Mapping(target = "cardCategory", source = "cardCategory")
    @Mapping(target = "cardType", source = "cardType")
    @Mapping(target = "cardStatus", source = "cardStatus")
    CardRequest toRequest(CardResponse cardResponse);

    /**
     * Maps CardRequest to CardResponse
     * @param cardRequest CardRequest
     * @return CardResponse
     */
    @Mapping(target = "cardNumber", source = "cardNumber")
    @Mapping(target = "cvv", source = "cvv")
    @Mapping(target = "pin", source = "pin")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "expirationDate", source = "expirationDate")
    @Mapping(target = "cardCategory", source = "cardCategory")
    @Mapping(target = "cardType", source = "cardType")
    @Mapping(target = "cardStatus", source = "cardStatus")
    CardResponse toResponse(CardRequest cardRequest);
}
