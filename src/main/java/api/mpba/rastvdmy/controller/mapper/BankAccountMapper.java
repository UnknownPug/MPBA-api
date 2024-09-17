package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.BankAccountRequest;
import api.mpba.rastvdmy.dto.response.BankAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "swift", source = "swift")
    @Mapping(target = "iban", source = "iban")
    BankAccountRequest toRequest(BankAccountResponse accountNumber);

    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "swift", source = "swift")
    @Mapping(target = "iban", source = "iban")
    BankAccountResponse toResponse(BankAccountRequest accountNumber);

}
