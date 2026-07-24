package com.mycom.myapp.domain.auth.service;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.auth.dto.PasswordResetConfirmRequest;
import com.mycom.myapp.domain.auth.dto.PasswordResetRequest;
import com.mycom.myapp.domain.auth.dto.ReissueRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String username);

    LoginResponse reissue(ReissueRequest request);

    void requestPasswordReset(PasswordResetRequest request);

    void confirmPasswordReset(PasswordResetConfirmRequest request);
}
