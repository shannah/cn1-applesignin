/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.auth.apple;

/**
 *
 * @author shannah
 */
public class AppleSignInException extends RuntimeException {
    private int code;
    private String errorMessage;
    
    public AppleSignInException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}
