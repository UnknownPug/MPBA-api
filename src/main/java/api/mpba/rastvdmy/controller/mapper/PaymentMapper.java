package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.PaymentRequest;
import api.mpba.rastvdmy.dto.response.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for PaymentRequest and PaymentResponse
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

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
