package com.dineout.book.util.enums;

public enum LoginType {

    FB("fb"), GOOGLE("google");

    private String serial;

    LoginType(String serial) {
        this.serial = serial;
    }
}
