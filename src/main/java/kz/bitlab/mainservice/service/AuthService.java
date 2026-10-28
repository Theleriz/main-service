package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.dto.LoginRequest;
import kz.bitlab.mainservice.dto.RefreshTokenRequest;
import kz.bitlab.mainservice.dto.TokenResponse;

public interface AuthService {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshTokenRequest request);
}
