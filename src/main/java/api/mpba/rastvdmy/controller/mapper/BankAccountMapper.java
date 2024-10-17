package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.BankAccountRequest;
import api.mpba.rastvdmy.dto.response.BankAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between {@link BankAccountRequest} and {@link BankAccountResponse}.
 * <p>
 * This interface utilizes MapStruct to automatically generate the implementation
 * for mapping fields between the two data transfer objects (DTOs).
 * The mappings ensure that fields are correctly transferred between request and response objects
 * for bank account operations.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    /**
     * Converts a {@link BankAccountResponse} object to a {@link BankAccountRequest} object.
     *
     * @param accountNumber The {@link BankAccountResponse} object to convert.
     * @return The converted {@link BankAccountRequest} object.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "iban", source = "iban")
    BankAccountRequest toRequest(BankAccountResponse accountNumber);

    /**
     * Converts a {@link BankAccountRequest} object to a {@link BankAccountResponse} object.
     *
     * @param accountNumber The {@link BankAccountRequest} object to convert.
     * @return The converted {@link BankAccountResponse} object.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "iban", source = "iban")
    BankAccountResponse toResponse(BankAccountRequest accountNumber);
}
