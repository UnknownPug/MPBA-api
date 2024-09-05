package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;

public interface AuthService {
    JwtAuthResponse signUp(UserRequest userRequest) throws Exception;

    JwtAuthResponse authenticate(UserLoginRequest loginRequest);
}
