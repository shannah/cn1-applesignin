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
public enum AppleCredentialState {
    Authorized,
    Revoked,
    NotFound;

    static AppleCredentialState getCredentialState(String state) {

        if ("authorized".equalsIgnoreCase(state)) {
            return Authorized;
        }
        if ("revoked".equalsIgnoreCase(state)) {
            return Revoked;
        }
        if ("notfound".equalsIgnoreCase(state)) {
            return NotFound;
        }
        throw new IllegalArgumentException("Unrecognized credential state " + state);

    }

    @Override
    public String toString() {
        switch (this) {
            case Authorized:
                return "Authorized";
            case Revoked:
                return "Revoked";
            case NotFound:
                return "NotFound";
        }
        return "Uknown state";
    }
    
    
}
