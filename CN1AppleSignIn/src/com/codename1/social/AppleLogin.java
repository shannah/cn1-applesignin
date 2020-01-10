/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.social;

import com.codename1.auth.apple.AppleCredential;
import com.codename1.auth.apple.AppleCredentialState;
import com.codename1.auth.apple.AppleSignIn;
import com.codename1.auth.apple.JWT;
import com.codename1.io.AccessToken;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.io.Oauth2;
import com.codename1.ui.Button;
import com.codename1.ui.CN;
import com.codename1.ui.Font;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.plaf.RoundRectBorder;
import com.codename1.ui.plaf.Style;
import com.codename1.util.AsyncResource;
import com.codename1.util.Base64;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

/**
 * 
 * @author shannah
 */
public class AppleLogin extends Login {

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        if (email == null) {
            AccessToken accessToken = getAccessToken();
            if (accessToken != null) {
                String identityToken = accessToken.getIdentityToken();
                if (identityToken != null) {
                    try {
                        Map claims = extractJWT(identityToken);
                        email = (String)claims.get("email");
                    } catch (Exception ex) {
                        Log.e(ex);
                    }
                }
                
            }
        }
            
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    

    public static final String OAUTH_URL = "https://appleid.apple.com/auth/authorize";
    public static final String TOKEN_REQUEST_URL = "https://appleid.apple.com/auth/token";
    private String privateKey;
    private String keyId;
    private String teamId;
    private String clientSecretUrl;
    
    private String fullName;
    private String userId;
    private String email;
    
    public AppleLogin() {
        setOauth2URL(OAUTH_URL);
        setScope("name email");
    }
    
    
    public void setPrivateKey(String key) {
        this.privateKey = key;
    }
    
    public String getPrivateKey() {
        return this.privateKey;
    }
    
    public String getKeyId() {
        return this.keyId;
    }
    
    public void setKeyId(String id) {
        this.keyId = id;
        
    }
    
    /**
     * @return the teamId
     */
    public String getTeamId() {
        return teamId;
    }

    /**
     * @param teamId the teamId to set
     */
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    /**
     * @return the clientSecretUrl
     */
    public String getClientSecretUrl() {
        return clientSecretUrl;
    }

    /**
     * @param clientSecretUrl the clientSecretUrl to set
     */
    public void setClientSecretUrl(String clientSecretUrl) {
        this.clientSecretUrl = clientSecretUrl;
    }
    
    @Override
    public boolean isNativeLoginSupported() {
        System.out.println("Checking if native login is supported");
        return AppleSignIn.getInstance().isSupported();
    }
    
    
    @Override
    protected boolean validateToken(String token) {
        if (isNativeLoginSupported()) {
            AppleCredential credential = AppleSignIn.getInstance().getCredential();
            if (credential != null) {
                try {
                    AppleCredentialState state = AppleSignIn.getInstance().getCredential().refreshAsync().get(5000);
                    return state == AppleCredentialState.Authorized;
                } catch (Throwable ex) {
                    return false;
                }
            } else {
                return false;
            }
                
        }
        AccessToken tok = getAccessToken();
        if (tok == null) {
            return false;
        }
        if (tok.isExpired()) {
            return false;
        }

        return true;

    }

    @Override
    public void nativelogin() {
        AppleSignIn.getInstance().loginAsync().onResult((credential, err) ->{
            System.out.println("in nativeLogin() onResult");
            if (err != null) {
                setAccessToken(null);
                
                callback.loginFailed(err.getMessage());
                return;
            }
            setAccessToken(new AccessToken(credential.getAuthorizationCode(), null, null, credential.getIdentityToken()));
            setEmail(credential.getEmail());
            setUserId(credential.getUser());
            setFullName(credential.getFullName());
            callback.loginSuccessful();
                    
        });
      
    }

    @Override
    public boolean nativeIsLoggedIn() {
        try {
            AppleCredentialState credential = AppleSignIn.getInstance().checkStateAsync().get(5000);
            return credential == AppleCredentialState.Authorized;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    @Override
    public void nativeLogout() {
        AppleSignIn.getInstance().logout();
    }
    
    

    @Override
    public void doLogin() {
        if (!isNativeLoginSupported()) {
            generateClientSecret();
        }
        super.doLogin(); 
    }
    
    
    private void generateClientSecret() {
        if (clientSecret == null) {
            
            if (clientSecretUrl != null) {
                try {
                    Map res = ConnectionRequest.fetchJSON(clientSecretUrl);
                    if (res != null && res.containsKey("client_secret")) {
                        clientSecret = (String)res.get("client_secret");
                    }
                } catch (IOException ex) {
                    Log.e(ex);
                }
            }
        }
        
        if (clientSecret == null) {
            
            if (keyId == null) {
                throw new IllegalStateException("Must set key ID to do Oauth2 with AppleLogin");
            }
            if (privateKey == null) {
                throw new IllegalStateException("Must set private key to do Oauth2 with AppleLogin");
            }
            if (teamId == null) {
                throw new IllegalStateException("Must set team ID to do Oauth2 with AppleLogin");
            }
            
            //https://developer.apple.com/documentation/signinwithapplerestapi/generate_and_validate_tokens
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
            
            try {
                clientSecret = jwt.generate().get();
            } catch (Throwable t) {
                Log.e(t);
            }
                
        }
    }
    
    
    private Map extractJWT(String idToken) throws IOException {
        idToken = idToken.substring(idToken.indexOf(".")+1);
        if (idToken.indexOf(".") > -1) {
            idToken = idToken.substring(0, idToken.indexOf("."));
        }
        try {
            
            System.out.println("idToken="+idToken);
            byte[] idTokenBytes = idToken.getBytes("UTF-8");
            byte[] decodedBytes = Base64.decode(idTokenBytes);
            System.out.println("Decoded bytes: "+Arrays.toString(decodedBytes));
            idToken = new String(decodedBytes, "UTF-8");
            JSONParser parser = new JSONParser();
            return parser.parseJSON(new StringReader(idToken));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Oauth2 createOauth2() {
        System.out.println("Creating oauth2");
        Hashtable params = new Hashtable();
        params.put("response_mode", "form_post");
        
        Oauth2 auth = new Oauth2(oauth2URL, clientId, redirectURI, scope, TOKEN_REQUEST_URL, clientSecret, params) {
            @Override
            protected void handleTokenRequestResponse(Map map) {
                super.handleTokenRequestResponse(map);
                if (map.containsKey("id_token")) {
                     try {
                        Map claims = extractJWT((String)map.get("id_token"));
                        setEmail((String)claims.get("email"));
                        
                        // This is a unique user ID that apple provides.  It doesn't
                        // mean anything but can be used to determine if the user
                        // has already logged in.
                        setUserId((String)claims.get("sub"));
                        setFullName(null);
                        
                    } catch (IOException ex) {
                        Log.e(ex);
                    }
                }
            }

            @Override
            protected void handleRedirectURLParams(Map params) {
                super.handleRedirectURLParams(params);
                
            }

            @Override
            public Oauth2.RefreshTokenRequest refreshToken(String refreshToken) {
                generateClientSecret();
                return super.refreshToken(refreshToken);
            }
            
            
            
            

        };
        auth.setUseBrowserWindow(true);
        return auth;
    }
    private static Font appleLogoFont;
    
    public static Font getAppleLogoFont() {
        if (appleLogoFont == null) {
            appleLogoFont = Font.createTrueTypeFont("apple-logo", "apple-logo.ttf");
        }
        return appleLogoFont;
    }
    
    public static final String APPLE_LOGO_CHAR = "\ue900";
    
    public static Image createAppleLogo(int color, float widthMM) {
        Font font = getAppleLogoFont();
        int w = CN.convertToPixels(widthMM);
        FontImage img = FontImage.createFixed(APPLE_LOGO_CHAR, font, color, w, w);
        return img;
    }
    
    public static void decorateLoginButton(Button btn, int bgColor, int fgColor) {
        btn.setText("Sign in with Apple");
        Style style = btn.getAllStyles();
        Image img = createAppleLogo(fgColor, 4.5f);
        btn.setIcon(img);
        btn.setPressedIcon(img);
        style.setFont(Font.createTrueTypeFont("native:MainRegular").derive(CN.convertToPixels(3.5f), 0));
        style.setPadding(2, 2, 2, 2);
        style.setPaddingUnit(Style.UNIT_TYPE_DIPS);
        style.setBgColor(bgColor);
        style.setFgColor(fgColor);
        style.setBgTransparency(0xff);
        style.setBorder(RoundRectBorder.create().cornerRadius(1.5f));
        double px1 = CN.convertToPixels(25.4f)/96.0;
        btn.setPreferredW((int)(px1 * 210));
        btn.setPreferredH((int)(px1 * 40));
        
        
    }
    
    
    
}
