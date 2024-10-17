package api.mpba.rastvdmy.controller.mapper;

import api.mpba.rastvdmy.dto.request.MessageRequest;
import api.mpba.rastvdmy.dto.response.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between {@link MessageRequest} and {@link MessageResponse}.
 * <p>
 * This interface uses MapStruct to automatically generate the implementation for mapping fields
 * between the two data transfer objects (DTOs) related to messages.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    /**
     * Maps a {@link MessageResponse} object to a {@link MessageRequest} object.
     *
     * @param messageResponse The {@link MessageResponse} object to be converted.
     * @return The converted {@link MessageRequest} object.
     */
    @Mapping(target = "receiverEmail", source = "receiverEmail")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "senderEmail", source = "senderEmail")
    @Mapping(target = "timestamp", source = "timestamp")
    MessageRequest toRequest(MessageResponse messageResponse);

    /**
     * Maps a {@link MessageRequest} object to a {@link MessageResponse} object.
     *
     * @param messageRequest The {@link MessageRequest} object to be converted.
     * @return The converted {@link MessageResponse} object.
     */
    @Mapping(target = "receiverEmail", source = "receiverEmail")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "senderEmail", source = "senderEmail")
    @Mapping(target = "timestamp", source = "timestamp")
    MessageResponse toResponse(MessageRequest messageRequest);
}
