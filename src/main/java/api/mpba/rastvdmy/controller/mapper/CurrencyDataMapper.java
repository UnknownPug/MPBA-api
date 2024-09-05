package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.CurrencyDataRequest;
import api.mpba.rastvdmy.dto.response.CurrencyDataResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CurrencyDataMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "rate", source = "rate")
    CurrencyDataResponse toResponse(CurrencyDataRequest currencyDataRequest);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "rate", source = "rate")
    CurrencyDataRequest toRequest(CurrencyDataResponse currencyDataResponse);
}
