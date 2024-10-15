package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;

public interface AuthService {
    JwtAuthResponse signUp(UserProfileRequest userProfileRequest) throws Exception;

    JwtAuthResponse authenticate(UserLoginRequest loginRequest);
}
