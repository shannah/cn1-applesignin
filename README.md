# Sign-in with Apple Codename One Library
Sign-in with Apple Support for Codename One

![Sign-in with Apple demo screenshot](https://github.com/shannah/cn1-applesignin/wiki/images/iOS-Screenshots.png "Sign-in with apple demo")

## Synopsis

This library adds support for Sign-in with Apple to Codename One apps.  On iOS, this uses the native Authentication framework.  On other platforms, it uses Apple's Oauth2 authentication.

## License

GPL2+Classpath Exception

## Getting Started

1. Install the CN1AppleSignin cn1lib using Codename One preferences.
2. Use the Apple Developer portal to configure your application to use Sign-in with Apple.  [See full setup instructions here](https://github.com/shannah/cn1-applesignin/wiki/Getting-Started)
3. Use the `AppleLogin` class (which is part of the CN1AppleSignin.cn1lib, as follows:

~~~~
AppleLogin login = new AppleLogin();

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
            updateAppleLogin();
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
