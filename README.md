# Sign-in with Apple Codename One Library
Sign-in with Apple Support for [Codename One](https://www.codenameone.com)

![Sign-in with Apple demo screenshot](https://github.com/shannah/cn1-applesignin/wiki/images/iOS-Screenshots.png "Sign-in with apple demo")

## Synopsis

This library adds support for Sign-in with Apple to [Codename One](https://www.codenameone.com) apps.  On iOS, this uses the native Authentication framework.  On other platforms, it uses Apple's Oauth2 authentication.

## License

GPL2+Classpath Exception

## Getting Started

1. Install the CN1AppleSignin cn1lib using Codename One preferences.
2. Use the Apple Developer portal to configure your application to use Sign-in with Apple.  [See full setup instructions here](https://github.com/shannah/cn1-applesignin/wiki/Getting-Started)
3. Use the `AppleLogin` class (which is part of the CN1AppleSignin.cn1lib, as follows:

~~~~
AppleLogin login = new AppleLogin();
// If using on non-iOS platforms, set Oauth2 settings here:
// login.setClientId(...);
// login.setKeyId(...);
// login.setTeamId(...);
// login.setRedirectURI(...);
// login.setPrivateKey(...);

if (login.isUserLoggedIn()) {
    new MainForm().show();
} else {
    new LoginForm().show();
}


....


class LoginForm extends Form {
    LoginForm() {
        super(BoxLayout.y());
        $(getContentPane()).setPaddingMillimeters(3f, 0, 0, 0);
        add(FlowLayout.encloseCenter(new Label(AppleLogin.createAppleLogo(0x0, 15f))));


        Button loginBtn = new Button("Sign in with Apple");
        AppleLogin.decorateLoginButton(loginBtn, 0x0, 0xffffff);

        loginBtn.addActionListener(evt->{
            login.doLogin(new LoginCallback() {
                @Override
                public void loginFailed(String errorMessage) {
                    System.out.println("Login failed");
                    ToastBar.showErrorMessage(errorMessage);
                }

                @Override
                public void loginSuccessful() {
                    new MainForm().show();
                }
            });
        });

        add(FlowLayout.encloseCenter(loginBtn));


    }
}


....

class MainForm extends Form {
    MainForm() {
        super(BoxLayout.y());
        add(new SpanLabel("You are now logged in as "+login.getEmail()));
        Button logout = new Button("Logout from Apple");
        logout.addActionListener(e->{
            login.doLogout();
            new LoginForm().show();
        });
        add(logout);
    }
}
~~~~

For full working demo, see the [Demo app](https://github.com/shannah/cn1-applesignin/tree/master/CN1AppleSignInDemo)

## Supported Platforms

This library will work on all platforms.  On iOS it will use native authentication.  On other platforms it will use Oauth2 authentication.

## Credits

* Created by [Steve Hannah](https://sjhannah.com)
* [Codename One](https://www.codenameone.com)

