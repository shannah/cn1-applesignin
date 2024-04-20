/*
 * Copyright comment here
 */
package com.codename1.auth.apple;

import com.codename1.system.NativeLookup;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.util.EventDispatcher;
import com.codename1.util.AsyncResource;

/**
 * This is a demo class to help you get started building a library
 *
 * @author Your name here
 */
public class AppleSignIn  {
    private boolean nativeStateChangeNotificationsInitialized;
    private AppleSignInNative peer;
    private static AppleSignIn instance;
    private LoginRequest pendingLoginRequest;
    private CheckCredentialsRequest pendingCheckCredentialsRequest;
    private EventDispatcher signinListeners = new EventDispatcher();
    private EventDispatcher stateChangeListeners = new EventDispatcher();
    private EventDispatcher checkCredentialsListeners = new EventDispatcher();
    private AppleCredential credential;

    private boolean credentialLoaded;
    
    private AppleSignIn() {
        peer = NativeLookup.create(AppleSignInNative.class);
    }
   
   
    CheckCredentialsRequest newCheckCredentialsRequest() {
        return new CheckCredentialsRequest();
    }
    

    
    public static AppleSignIn getInstance() {
        if (instance == null) {
            instance = new AppleSignIn();
        }
        return instance;
    }
    
    public boolean isSupported() {
        System.out.println("Checking if signing supported "+peer);
        return peer != null && peer.isSupported();
    }
            
    public void logout() {
        setCredential(null);
    }
    
    
    private synchronized void setCredential(AppleCredential credential) {
        this.credential = credential;
        AppleCredential.saveInStorage(credential);
    }
    
    public synchronized AppleCredential getCredential() {
        if (!credentialLoaded && this.credential == null) {
            this.credential = AppleCredential.loadFromStorage();
        }
        return this.credential;
    }
    
    
    
    public class LoginRequest extends AsyncResource<AppleCredential> {

        @Override
        public void complete(AppleCredential value) {
            if (pendingLoginRequest == this) {
                pendingLoginRequest = null;
            }
            super.complete(value);
        }

        @Override
        public void error(Throwable t) {
            if (pendingLoginRequest == this) {
                pendingLoginRequest = null;
            }
            super.error(t);
        }
        
        
        
    }
    /**
     * Initiates a login request.  This will open a login dialog and ask the user
     * to login.
     * @return 
     */
    public LoginRequest loginAsync() {
        return loginAsync(new LoginRequest());
    }
    
    private LoginRequest loginAsync(final LoginRequest out) {
        if (out.isDone()) {
            return out;
        }
        if (pendingLoginRequest != null && pendingLoginRequest != out) {
            pendingLoginRequest.addListener(out);
            return out;
        }
    
        class LoginListener implements ActionListener<SignInEvent> {

            @Override
            public void actionPerformed(SignInEvent evt) {
                System.out.println("In loginListener.actionPerrofmed");
                signinListeners.removeListener(this);
                if (!out.isDone()) {
                    if (evt.getError() != null) {
                        out.error(evt.getError());
                        return;
                    }
                    System.out.println("firing out.complete()");
                    out.complete(evt.credential);
                    
                }
            }
            
        };
        LoginListener listener = new LoginListener();
        System.out.println("Adding signinlistener");
        signinListeners.addListener(listener);
        peer.doLogin();
         
        return out;
        
    }
    public static class SignInEvent extends ActionEvent {
        private AppleCredential credential;
        private AppleCredentialState state;
        private AppleSignInException error;
        
        public SignInEvent(AppleSignIn source, AppleSignInException error) {
            super(source);
            this.error = error;
        }
        
        public SignInEvent(AppleSignIn source, AppleCredentialState state) {
            super(source);
            this.state = state;
        }
        
        public SignInEvent(AppleSignIn source, AppleCredential credential) {
            super(source);
            this.credential = credential;
            this.state = credential.getState();
        }
        
        public AppleCredentialState getState() {
            return state;
        }
        
        public AppleSignInException getError() {
            return error;
        }
        
    }
    static void fireSigninEvent(String identityToken, String authorizationCode, String userId, String email, String name, String state) {
        System.out.println("Firing signin event for state "+state);
        AppleCredentialState credentialState = AppleCredentialState.getCredentialState(state);
        System.out.println("Credential state: "+credentialState);
        if (credentialState == AppleCredentialState.Authorized) {
            AppleCredential previousCredential = instance.getCredential();
            // Apple only provides email and name on first login, so we need to merge the previous credential
            String previousEmail = previousCredential == null ? "" : previousCredential.getEmail();
            String previousName = previousCredential == null ? "" : previousCredential.getFullName();
            AppleCredential credential = new AppleCredential(
                    identityToken,
                    authorizationCode,
                    credentialState,
                    userId,
                    name != null && !name.isEmpty() ? name : previousName,
                    email != null && !email.isEmpty() ? email : previousEmail
            );

            instance.setCredential(credential);
            instance.signinListeners.fireActionEvent(new SignInEvent(instance, credential));
        } else {
            instance.setCredential(null);
            instance.signinListeners.fireActionEvent(new SignInEvent(instance, credentialState));
        }
        
    }
    
    static void fireSignInErrorEvent(int errorCode, String errorMessage) {
        instance.signinListeners.fireActionEvent(new SignInEvent(instance, new AppleSignInException(errorCode, errorMessage)));
    }
    
    // State change notifications------------------------------------------------
    
    public static class StateChangeEvent extends ActionEvent {
        private AppleCredentialState state;
        public StateChangeEvent(AppleSignIn source, AppleCredentialState state) {
            super(source);
        }
    }
    private void initializeStateChangeNotifications() {
        if (!nativeStateChangeNotificationsInitialized) {
            nativeStateChangeNotificationsInitialized = true;
        }
        peer.initializeStateChangeNotifications();
    }
    public void addStateChangeListener(ActionListener<StateChangeEvent> l) {
        initializeStateChangeNotifications();
        stateChangeListeners.addListener(l);
    }
    
    public void removeStateChangeListener(ActionListener<StateChangeEvent> l) {
        stateChangeListeners.removeListener(l);
    }
    static void fireStateChangeEvent(String state) {
        AppleCredentialState credentialState = AppleCredentialState.getCredentialState(state);
        switch (credentialState) {
            case NotFound:
            case Revoked:
                instance.setCredential(null);
                
        }
        instance.stateChangeListeners.fireActionEvent(new StateChangeEvent(instance, AppleCredentialState.getCredentialState(state)));
    }
    
    // End state change notifications --------------------------------------------
    
    // Check Credentials Requests ------------------------------------------------
    public class CheckCredentialsRequest extends AsyncResource<AppleCredentialState> {

        @Override
        public void complete(AppleCredentialState value) {
            if (pendingCheckCredentialsRequest == this) {
                pendingCheckCredentialsRequest = null;
            }
            super.complete(value);
        }

        @Override
        public void error(Throwable t) {
            if (pendingCheckCredentialsRequest == this) {
                pendingCheckCredentialsRequest = null;
            }
            super.error(t);
        }
    }
    public static class CheckCredentialStateEvent extends ActionEvent {
        private AppleCredentialState state;
        private AppleSignInException error;
        
        public CheckCredentialStateEvent(AppleSignIn source, AppleSignInException error) {
            super(source);
            this.error = error;
        }
        
        public CheckCredentialStateEvent(AppleSignIn source, AppleCredentialState state) {
            super(source);
            this.state = state;
        }
        
        public CheckCredentialStateEvent(AppleSignIn source, int errorCode, String errorMessage) {
            super(source);
            error = new AppleSignInException(errorCode, errorMessage);
        }
        
        public AppleSignInException getError() {
            return error;
        }
    }
    static void fireCheckCredentialState(String state) {
        instance.checkCredentialsListeners.fireActionEvent(new CheckCredentialStateEvent(instance, AppleCredentialState.getCredentialState(state)));
    }
    
    static void fireCheckCredentialStateError(int code, String message) {
        instance.checkCredentialsListeners.fireActionEvent(new CheckCredentialStateEvent(instance, code, message));
    }
    
    public CheckCredentialsRequest checkStateAsync() {
        AppleCredential credential = getCredential();
        if (credential == null) {
            CheckCredentialsRequest out = new CheckCredentialsRequest();
            out.complete(AppleCredentialState.NotFound);
            return out;
        }
        return checkStateAsync(credential);
    }
    
    public CheckCredentialsRequest checkStateAsync(AppleCredential credential) {
        return checkStateAsync(credential, new CheckCredentialsRequest());
    }
    
    CheckCredentialsRequest checkStateAsync(AppleCredential credential, CheckCredentialsRequest out) {
        if (out.isDone()) {
            return out;
        }
        if (pendingCheckCredentialsRequest != null && pendingCheckCredentialsRequest != out) {
            
            pendingCheckCredentialsRequest.addListener(out);
            return out;
        }
        
        class RefreshListener implements ActionListener<CheckCredentialStateEvent> {

            @Override
            public void actionPerformed(CheckCredentialStateEvent evt) {
                checkCredentialsListeners.removeListener(this);
                if (out.isDone()) {
                    return;
                }
                if (evt.getError() != null) {
                    out.error(evt.getError());
                    return;
                }
                switch (evt.state) {
                    case NotFound:
                    case Revoked:
                        setCredential(null);
                        
                }
                out.complete(evt.state);
            }
            
        };
        RefreshListener l = new RefreshListener();
        checkCredentialsListeners.addListener(l);
        peer.getCredentialState(credential.getUser());
        return out;
        
    }
    
    // End Check Credentials Requests -------------------------------------------
}
