package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.CurrencyDataRequest;
import api.mpba.rastvdmy.dto.response.CurrencyDataResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between {@link CurrencyDataRequest} and {@link CurrencyDataResponse}.
 * <p>
 * This interface utilizes MapStruct to automatically generate the implementation
 * for mapping fields between the two data transfer objects (DTOs) related to currency data.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface CurrencyDataMapper {

    /**
     * Maps a {@link CurrencyDataRequest} object to a {@link CurrencyDataResponse} object.
     *
     * @param currencyDataRequest The {@link CurrencyDataRequest} object to be converted.
     * @return The converted {@link CurrencyDataResponse} object.
     */
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "rate", source = "rate")
    CurrencyDataResponse toResponse(CurrencyDataRequest currencyDataRequest);

    /**
     * Maps a {@link CurrencyDataResponse} object to a {@link CurrencyDataRequest} object.
     *
     * @param currencyDataResponse The {@link CurrencyDataResponse} object to be converted.
     * @return The converted {@link CurrencyDataRequest} object.
     */
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "rate", source = "rate")
    CurrencyDataRequest toRequest(CurrencyDataResponse currencyDataResponse);
}
