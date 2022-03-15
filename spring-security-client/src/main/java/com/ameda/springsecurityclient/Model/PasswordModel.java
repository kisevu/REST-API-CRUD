package com.ameda.springsecurityclient.Model;

import lombok.Data;

@Data
public class PasswordModel {
    private String email;
    private String password;
    private String newPassword;
}
