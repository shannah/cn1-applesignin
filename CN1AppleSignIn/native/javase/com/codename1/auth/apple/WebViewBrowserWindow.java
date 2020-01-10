package com.codename1.auth.apple;
import ca.weblite.webview.WebViewCLIClient;
import ca.weblite.webview.WebViewClient;
import com.codename1.impl.javase.*;
import com.codename1.io.Log;
import com.codename1.ui.BrowserWindow;
import com.codename1.ui.events.ActionEvent;
/**
 * A wrapper for the native webview window.  This will use the native browser window
 * of the platform (WebKit on MacOS and Linux, and either EDGE Chromium, or EDGE on Windows).
 * Uses https://github.com/shannah/webviewjar
 * @author shannah
 */
public class WebViewBrowserWindow extends AbstractBrowserWindowSE {
    
    WebViewCLIClient webview;
    WebViewCLIClient.Builder builder;
    private boolean closed;
    
    public WebViewBrowserWindow(String startURL) {
        System.out.println("Creating new browser window for "+startURL);
        builder = (WebViewCLIClient.Builder)new WebViewCLIClient.Builder().url(startURL);
    }
    
    
    
    public void show() {
        if (webview == null) {
            webview = builder.build();
            webview.addLoadListener(new WebViewClient.WebEventListener<WebViewClient.OnLoadWebEvent>() {
                @Override
                public void handleEvent(WebViewClient.OnLoadWebEvent evt) {
                    fireLoadEvent(new ActionEvent(evt.getURL()));
                }
            });
            
            
        }
    }

    public void setSize(final int width, final int height) {
        
        builder.size(width, height);
    }
    
    public void setTitle(final String title) {
        builder.title(title);
    }

    public void hide() {
        if (!closed) {
            closed = true;
            if (webview != null) {
                try {
                    webview.close();
                } catch (Exception ex) {
                    Log.e(ex);
                }
            }
        }
    }

    public void cleanup() {
        hide();
    }

    public void eval(BrowserWindow.EvalRequest req) {
        if (webview != null) {
            webview.eval(req.getJS()).thenAccept(str->{
                if (!req.isDone()) {
                    req.complete(str);
                }
            });
        }
    }
}
