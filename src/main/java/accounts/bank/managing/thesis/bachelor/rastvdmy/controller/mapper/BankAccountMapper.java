package accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankAccountRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.BankAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "swift", source = "swift")
    @Mapping(target = "iban", source = "iban")
    BankAccountRequest toRequest(BankAccountResponse accountNumber);

    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "swift", source = "swift")
    @Mapping(target = "iban", source = "iban")
    BankAccountResponse toResponse(BankAccountRequest accountNumber);

}
