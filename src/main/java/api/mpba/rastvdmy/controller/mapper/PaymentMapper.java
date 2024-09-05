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

    /**
     * Maps PaymentResponse to PaymentRequest
     * @param paymentResponse PaymentResponse
     * @return PaymentRequest
     */
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "dateTime", source = "dateTime")
    @Mapping(target = "type", source = "paymentType")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "recipientName", source = "recipientName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "senderNumber", source = "senderNumber")
    @Mapping(target = "recipientNumber", source = "recipientNumber")
    @Mapping(target = "senderPin", source = "senderPin")
    @Mapping(target = "senderCvv", source = "senderCvv")
    PaymentRequest toRequest(PaymentResponse paymentResponse);

    /**
     * Maps PaymentRequest to PaymentResponse
     * @param paymentRequest PaymentRequest
     * @return PaymentResponse
     */
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "dateTime", source = "dateTime")
    @Mapping(target = "paymentType", source = "type")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "recipientName", source = "recipientName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "senderNumber", source = "senderNumber")
    @Mapping(target = "recipientNumber", source = "recipientNumber")
    @Mapping(target = "senderPin", source = "senderPin")
    @Mapping(target = "senderCvv", source = "senderCvv")
    PaymentResponse toResponse(PaymentRequest paymentRequest);
}
