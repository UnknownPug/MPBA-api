package accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CurrencyDataRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.CurrencyDataResponse;
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
