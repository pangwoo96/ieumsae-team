package com.ieumsae.domain;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {
    private String userId;
    private String userName;
    private String userNickName;
    private String userPw;
    private String userEmail;
    private boolean socialLogin;
}
