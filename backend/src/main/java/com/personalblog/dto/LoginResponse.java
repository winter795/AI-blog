package com.personalblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String nickname;
    private String avatar;
    private String role;
}
