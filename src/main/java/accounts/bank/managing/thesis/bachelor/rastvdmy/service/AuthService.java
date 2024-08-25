package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserLoginRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.JwtAuthResponse;

public interface AuthService {
    JwtAuthResponse signUp(UserRequest userRequest) throws Exception;

    JwtAuthResponse authenticate(UserLoginRequest loginRequest);
}
