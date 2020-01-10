/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.auth.apple.tests;

import com.codename1.auth.apple.JWT;
import com.codename1.auth.apple.demos.signin.AppleSigninDemo;
import static com.codename1.auth.apple.demos.signin.AppleSigninDemo.AppleClientID;
import com.codename1.io.Log;
import com.codename1.testing.AbstractTest;

/**
 *
 * @author shannah
 */
public class JWTTests extends AbstractTest {

    @Override
    public boolean shouldExecuteOnEDT() {
        return true;
    }

    
    
    @Override
    public boolean runTest() throws Exception {
        String clientId = AppleClientID;
        String keyId = AppleSigninDemo.AppleKeyID;
        String teamId = AppleSigninDemo.AppleTeamID;
        String privateKey = AppleSigninDemo.ApplePrivateKey;
        String clientSecret;
        JWT jwt = new JWT();
            
        jwt.putHeader("kid", keyId)
                .putHeader("alg", "ES256")
                .put("iss", teamId)
                .put("iat", (int)(System.currentTimeMillis() / 1000l))
                .put("exp", (int)(System.currentTimeMillis() / 1000l) + 15777000)
                .put("aud", "https://appleid.apple.com")
                .put("sub", clientId)
                .privateKey(privateKey)
                ;

        
        clientSecret = jwt.generate().get(5000);
        System.out.println("Client secret is "+clientSecret);
        assertNotNull(clientSecret, "Client secret should not be null");
        return true;
    }
    
}
