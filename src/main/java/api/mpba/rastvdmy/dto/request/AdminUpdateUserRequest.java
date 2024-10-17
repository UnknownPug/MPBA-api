package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Transfer Object (DTO) that represents a request to update a user's
 * details by an administrator. This record is used to encapsulate the fields
 * needed for the update operation, ensuring type safety and immutability.
 *
 * <p>The following fields are included:</p>
 *
 * <ul>
 *   <li><b>surname</b>: The surname of the user to be updated.</li>
 *   <li><b>countryOfOrigin</b>: The country of origin of the user, represented
 *       as a string. This field is serialized to/from JSON with the key
 *       "country_of_origin".</li>
 * </ul>
 *
 * @param surname         The surname of the user.
 * @param countryOfOrigin The country of origin of the user.
 */
public record AdminUpdateUserRequest(
        String surname,

        @JsonProperty("country_of_origin")
        String countryOfOrigin
) {}
