package com.codename1.auth.apple;

import com.codename1.impl.javase.AbstractBrowserWindowSE;
import com.codename1.impl.javase.BrowserWindowFactory;
import com.codename1.impl.javase.JavaSEPort;
import com.codename1.social.AppleLogin;

public class AppleSignInNativeImpl implements com.codename1.auth.apple.AppleSignInNative{
    
    private static boolean factoryInstalled;
    
    /**
     * Installs a custom factory for creating webviews.  This will use the default factory in
     * most cases, but will use our custom webview for apple's OAUTH login.  This is because
     * Apple's OAUTH login doesn't appear to work with the JavaFX webview.
     */
    private static void installFactory() {
        if (factoryInstalled) {
            return;
        }
        factoryInstalled = true;
        System.out.println("Installing webview factory");
        final BrowserWindowFactory existingFactory = JavaSEPort.instance.getBrowserWindowFactory();
        JavaSEPort.instance.setBrowserWindowFactory(new BrowserWindowFactory() {
            @Override
            public AbstractBrowserWindowSE createBrowserWindow(String startURL) {
                System.out.println("Creating browser window for "+startURL);
                if (startURL.startsWith(AppleLogin.OAUTH_URL)) {
                    System.out.print("WebViewBrowserWindow here we are");
                    return new WebViewBrowserWindow(startURL);
                    
                }
                return existingFactory.createBrowserWindow(startURL);
            }
            
        });
    }
    
    public void doLogin() {
    }

    public void initializeStateChangeNotifications() {
    }

    public void getCredentialState(String param) {
    }

    public boolean isSupported() {
        System.out.println("isSupported?");
        // This is a bit sneaky.  In JavaSE, we don't want to use native login support
        // We want to use Oauth2 so we officially return that we do not support
        // Apple signin natively here.  HOWEVER, Apple Login doesn't seem to work
        // with the JavaFX webview, so we need to install the custom WebView factory
        // so that it uses the bundled webview instead.
        installFactory();
        return false;
    }
    

}
