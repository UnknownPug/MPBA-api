package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * This class represents the response from the currency exchange rate API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyApiResponse {

    /**
     * The result of the API call (e.g., "success").
     */
    private String result;

    /**
     * The URL to the API documentation.
     */
    private String documentation;

    /**
     * The terms of use for the API.
     */
    @JsonProperty("terms_of_use")
    private String termsOfUse;

    /**
     * The Unix timestamp of the last update time.
     */
    @JsonProperty("time_last_update_unix")
    private long timeLastUpdateUnix;

    /**
     * The UTC time of the last update.
     */
    @JsonProperty("time_last_update_utc")
    private String timeLastUpdateUtc;

    /**
     * The Unix timestamp of the next update time.
     */
    @JsonProperty("time_next_update_unix")
    private long timeNextUpdateUnix;

    /**
     * The UTC time of the next update.
     */
    @JsonProperty("time_next_update_utc")
    private String timeNextUpdateUtc;

    /**
     * The base currency code.
     */
    @JsonProperty("base_code")
    private String baseCode;

    /**
     * A map of currency codes to their conversion rates.
     */
    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;

    /**
     * The conversion rate of the currency.
     */
    @JsonProperty("conversion_rate")
    private String conversionRate;
}