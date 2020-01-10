/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.auth.apple;

import com.codename1.io.Externalizable;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author shannah
 */
public class AppleCredential implements Externalizable {
    private static final String KEY_CREDENTIAL = "com.codename1.social.AppleSignIn.Credential";

    static synchronized void saveInStorage(AppleCredential credential) {
        Storage s = Storage.getInstance();
        s.writeObject(KEY_CREDENTIAL, credential);

    }

    static synchronized AppleCredential loadFromStorage() {
        Util.register(new AppleCredential());
        Storage s = Storage.getInstance();
        return (AppleCredential)s.readObject(KEY_CREDENTIAL);
    }

    /**
     * @return the identityToken
     */
    public String getIdentityToken() {
        return identityToken;
    }

    /**
     * @return the authorizationCode
     */
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    /**
     * @return the state
     */
    public AppleCredentialState getState() {
        return state;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    private String identityToken;
    private String authorizationCode;
    private AppleCredentialState state;
    private String user;
    private String fullName;
    private String email;


    public AppleCredential() {

    }

    public AppleCredential(String identityToken, String authorizationCode, AppleCredentialState state, String user, String fullName, String email) {
        this.identityToken = identityToken;
        this.authorizationCode = authorizationCode;
        this.state = state;
        this.user = user;
        this.fullName = fullName;
        this.email = email;
    }



    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void externalize(DataOutputStream out) throws IOException {
        Util.writeUTF(getIdentityToken(), out);
        Util.writeUTF(getAuthorizationCode(), out);
        Util.writeUTF(getState().toString(), out);
        Util.writeUTF(getUser(), out);
        Util.writeUTF(getFullName(), out);
        Util.writeUTF(getEmail(), out);
    }



    @Override
    public void internalize(int version, DataInputStream in) throws IOException {
        identityToken = Util.readUTF(in);
        authorizationCode = Util.readUTF(in);
        state = AppleCredentialState.getCredentialState(Util.readUTF(in));
        user = Util.readUTF(in);
        fullName = Util.readUTF(in);
        email = Util.readUTF(in);
    }

    @Override
    public String getObjectId() {
        return "com.codename1.social.AppleSignIn.Credential";
    }

    public AppleSignIn.CheckCredentialsRequest refreshAsync() {
        return refreshAsync(instance().newCheckCredentialsRequest());
    }

    private AppleSignIn.CheckCredentialsRequest refreshAsync(AppleSignIn.CheckCredentialsRequest out) {
        return instance().checkStateAsync(this, out);
    }
    
    private AppleSignIn instance() {
        return AppleSignIn.getInstance();
    }
}
