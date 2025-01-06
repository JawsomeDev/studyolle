package com.studyolle.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AccountRole {
    ADMIN("관리자"), CUSTOMER("고객");

    private String value;
}
