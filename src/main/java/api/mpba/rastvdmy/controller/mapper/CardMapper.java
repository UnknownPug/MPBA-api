package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.CardRequest;
import api.mpba.rastvdmy.dto.response.CardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between {@link CardRequest} and {@link CardResponse}.
 * <p>
 * This interface utilizes MapStruct to automatically generate the implementation
 * for mapping fields between the two data transfer objects (DTOs) related to cards.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface CardMapper {

    /**
     * Maps a {@link CardResponse} object to a {@link CardRequest} object.
     *
     * @param cardResponse The {@link CardResponse} object to be converted.
     * @return The converted {@link CardRequest} object.
     */
    @Mapping(target = "id", source = "id")
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
     * Maps a {@link CardRequest} object to a {@link CardResponse} object.
     *
     * @param cardRequest The {@link CardRequest} object to be converted.
     * @return The converted {@link CardResponse} object.
     */
    @Mapping(target = "id", source = "id")
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
