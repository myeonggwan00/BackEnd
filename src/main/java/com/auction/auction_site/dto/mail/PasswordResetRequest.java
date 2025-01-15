package com.auction.auction_site.dto.mail;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String token;
    private String newPassword;
}
