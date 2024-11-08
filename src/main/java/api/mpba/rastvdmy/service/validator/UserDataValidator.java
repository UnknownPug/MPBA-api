package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A validator class that validates user data fields using different validation strategies.
 */
@Component
public class UserDataValidator {
    private final Map<String, ValidationStrategy> validationStrategies;

    /**
     * Constructor for UserDataValidator.
     *
     * @param countryValidationStrategy The strategy for validating country fields.
     * @
     */
    @Autowired
    public UserDataValidator(CountryValidation countryValidationStrategy) {
        this.validationStrategies = new HashMap<>();
        validationStrategies.put("name", new NameValidation());
        validationStrategies.put("surname", new SurnameValidation());
        validationStrategies.put("email", new EmailValidation());
        validationStrategies.put("password", new PasswordValidation());
        validationStrategies.put("phoneNumber", new PhoneNumValidation());
        validationStrategies.put("dateOfBirth", new DateOfBirthValidation());
        validationStrategies.put("country", countryValidationStrategy);
    }

    /**
     * Validates a specific field using the appropriate validation strategy.
     *
     * @param fieldName The name of the field to validate.
     * @param value     The value of the field to validate.
     * @throws ApplicationException if the field is invalid or no validation strategy is found.
     */
    public void validateField(String fieldName, String value) throws ApplicationException {
        ValidationStrategy strategy = validationStrategies.get(fieldName);
        if (strategy != null) {
            strategy.validate(value);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "No validation strategy for field: " + fieldName);
        }
    }
}