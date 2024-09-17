package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.MessageRequest;
import api.mpba.rastvdmy.dto.response.MessageResponse;
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
    @Mapping(target = "receiverEmail", source = "receiverEmail")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "senderEmail", source = "senderEmail")
    @Mapping(target = "timestamp", source = "timestamp")
    MessageRequest toRequest(MessageResponse messageResponse);

    /**
     * Maps MessageRequest to MessageResponse
     * @param messageRequest MessageRequest
     * @return MessageResponse
     */
    @Mapping(target = "receiverEmail", source = "receiverEmail")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "senderEmail", source = "senderEmail")
    @Mapping(target = "timestamp", source = "timestamp")
    MessageResponse toResponse(MessageRequest messageRequest);
}
