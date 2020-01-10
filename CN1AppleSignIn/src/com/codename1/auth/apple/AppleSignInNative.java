/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.auth.apple;

import com.codename1.system.NativeInterface;
import com.codename1.ui.PeerComponent;

/**
 *
 * @author shannah
 */
public interface AppleSignInNative extends NativeInterface {
    public void getCredentialState(String userID);
    public void doLogin();
    public void initializeStateChangeNotifications();
    
}
