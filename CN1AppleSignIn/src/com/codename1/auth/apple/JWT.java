/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.auth.apple;

import com.codename1.io.Util;
import com.codename1.processing.Result;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.CN;
import com.codename1.util.AsyncResource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shannah
 */
public class JWT {
    private Map<String,Object> payload = new HashMap<>();
    private Map<String,Object> header = new HashMap<>();
    private String privateKey;
    
    public class JWTGeneration extends AsyncResource<String> {
        
    }
    
    public JWT put(String key, String value) {
        payload.put(key, value);
        return this;
    }
    
    public JWT put(String key, int value) {
        payload.put(key, value);
        return this;
    }
    
    public JWT putHeader(String key, String value) {
        header.put(key, value);
        return this;
    }
    
    public JWT privateKey(String key) {
        this.privateKey = key;
        return this;
    }
    
    public JWTGeneration generate() {
        return generate(new JWTGeneration());
    }
    private JWTGeneration generate(final JWTGeneration out) {
        if (!CN.isEdt()) {
            CN.callSerially(()->{
                generate(out);
            });
            return out;
        }
        try {
            System.out.println("Creating browser component");
            BrowserComponent cmp = new BrowserComponent();
            String html = Util.readToString(CN.getResourceAsStream("/com.codename1.auth.apple.GenerateJWT.html"));
           // System.out.println("Setting page to "+html);
            cmp.setPage(html, null);
            cmp.addWebEventListener("onLoad", evt->{
                System.out.println("In onLoad event of browser");
                String payLoadStr = Result.fromContent(payload).toString();
                String headerStr = Result.fromContent(header).toString();
                
                cmp.execute("window._genJWS(${0}, ${1}, ${2}, function(result, err) {"
                        + "    if (err === null) {"
                        + "       callback.onSuccess(result);"
                        + "    } else {"
                        + "       "
                        + "       callback.onSuccess(err);"
                        + "    }"
                        + "})", new Object[]{
                            headerStr,
                            payLoadStr,
                            privateKey
                        }, res->{
                            if (res.isNull()) {
                                out.complete(null);
                            } else {
                                out.complete(res.getValue());
                            }
                        });
            });
        } catch (IOException ex) {
            out.error(ex);
        }
        return out;
        
    }
}
