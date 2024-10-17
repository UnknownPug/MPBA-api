package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.PaymentRequest;
import api.mpba.rastvdmy.dto.response.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between {@link PaymentRequest} and {@link PaymentResponse}.
 * <p>
 * This interface uses MapStruct to automatically generate the implementation for mapping fields
 * between the two data transfer objects (DTOs) related to payments.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Maps a {@link PaymentResponse} object to a {@link PaymentRequest} object.
     *
     * @param paymentResponse The {@link PaymentResponse} object to be converted.
     * @return The converted {@link PaymentRequest} object.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "recipientName", source = "recipientName")
    @Mapping(target = "dateTime", source = "dateTime")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "currency", source = "currency")
    PaymentRequest toRequest(PaymentResponse paymentResponse);

    /**
     * Maps a {@link PaymentRequest} object to a {@link PaymentResponse} object.
     *
     * @param paymentRequest The {@link PaymentRequest} object to be converted.
     * @return The converted {@link PaymentResponse} object.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "recipientName", source = "recipientName")
    @Mapping(target = "dateTime", source = "dateTime")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "currency", source = "currency")
    PaymentResponse toResponse(PaymentRequest paymentRequest);
}
