package api.mpba.rastvdmy.dto.request;

public record UserLoginRequest(
        String email,

        String password
) {}
