package accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.MessageRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for MessageRequest and MessageResponse
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    /**
     * Maps MessageResponse to MessageRequest
     * @param messageResponse MessageResponse
     * @return MessageRequest
     */
    @Mapping(target = "receiverName", source = "receiverName")
    @Mapping(target = "content", source = "content")
    MessageRequest toRequest(MessageResponse messageResponse);

    /**
     * Maps MessageRequest to MessageResponse
     * @param messageRequest MessageRequest
     * @return MessageResponse
     */
    @Mapping(target = "receiverName", source = "receiverName")
    @Mapping(target = "content", source = "content")
    MessageResponse toResponse(MessageRequest messageRequest);
}
