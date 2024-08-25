package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import javax.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank(message = "Email cannot be empty")
        String email,

        @NotBlank(message = "Password cannot be empty")
        String password
) {}
