#import "com_codename1_auth_apple_AppleSignInNativeImpl.h"
#import <AuthenticationServices/AuthenticationServices.h>
#import "CodenameOne_GLViewController.h"
#import "com_codename1_auth_apple_AppleSignIn.h"

@implementation com_codename1_auth_apple_AppleSignInNativeImpl

-(void)doLogin{
    if (@available(iOS 13.0, *)) {
        dispatch_async(dispatch_get_main_queue(), ^{
            POOL_BEGIN();
            // A mechanism for generating requests to authenticate users based on their Apple ID.
            ASAuthorizationAppleIDProvider *appleIDProvider = [ASAuthorizationAppleIDProvider new];

            // Creates a new Apple ID authorization request.
            ASAuthorizationAppleIDRequest *request = appleIDProvider.createRequest;
            // The contact information to be requested from the user during authentication.
            request.requestedScopes = @[ASAuthorizationScopeFullName, ASAuthorizationScopeEmail];

            // A controller that manages authorization requests created by a provider.
            ASAuthorizationController *controller = [[ASAuthorizationController alloc] initWithAuthorizationRequests:@[request]];

            // A delegate that the authorization controller informs about the success or failure of an authorization attempt.
            controller.delegate = self;

            // A delegate that provides a display context in which the system can present an authorization interface to the user.
            controller.presentationContextProvider = self;

            // starts the authorization flows named during controller initialization.
            [controller performRequests];
            POOL_END();
        });
    }
    
}

static void fireStateChanged(NSString *state) {
    com_codename1_auth_apple_AppleSignIn_fireStateChangeEvent___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG state)
    );
}

static void fireCheckState(NSString *state) {
    com_codename1_auth_apple_AppleSignIn_fireCheckCredentialState___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG state)
    );
}
static void fireCheckStateError(int errorCode, NSString *errorMessage) {
    
    com_codename1_auth_apple_AppleSignIn_fireCheckCredentialState___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG
         fromNSString(CN1_THREAD_GET_STATE_PASS_ARG errorMessage)
    );
}

-(void)initializeStateChangeNotifications{
    if (@available(iOS 13.0, *)) {
        dispatch_async(dispatch_get_main_queue(), ^{
            POOL_BEGIN();
            NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
            [center addObserver:self selector:@selector(handleSignInWithAppleStateChanged:) name:ASAuthorizationAppleIDProviderCredentialRevokedNotification object:nil];
            POOL_END();
        });
    }
}

-(void)handleSignInWithAppleStateChanged:(id)noti {
    fireStateChanged(@"Revoked");
}
//static void fireSigninEvent(String identityToken, String authorizationCode, String userId, String email, String name, String state) {
API_AVAILABLE(ios(13.0))
static void fireLoginEvent(ASAuthorizationAppleIDCredential *appleIDCredential) {
    NSPersonNameComponentsFormatter *formatter = [[NSPersonNameComponentsFormatter alloc] init];
    formatter.style = NSPersonNameComponentsFormatterStyleMedium;
    NSString *fullName = [formatter stringFromPersonNameComponents:appleIDCredential.fullName];

    com_codename1_auth_apple_AppleSignIn_fireSigninEvent___java_lang_String_java_lang_String_java_lang_String_java_lang_String_java_lang_String_java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG  [[NSString alloc] initWithData:appleIDCredential.identityToken encoding:NSUTF8StringEncoding]),
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG  [[NSString alloc] initWithData:appleIDCredential.authorizationCode encoding:NSUTF8StringEncoding]),
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG appleIDCredential.user),
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG appleIDCredential.email),
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG fullName),
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Authorized")
    );
}


static void fireSignInError(int errorCode, NSString* errorMessage) {
    
  com_codename1_auth_apple_AppleSignIn_fireSignInErrorEvent___int_java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG
        errorCode,
        fromNSString(CN1_THREAD_GET_STATE_PASS_ARG errorMessage)
    );
}

- (void)authorizationController:(ASAuthorizationController *)controller didCompleteWithAuthorization:(ASAuthorization *)authorization  API_AVAILABLE(ios(13.0)){

    NSLog(@"%s", __FUNCTION__);
    NSLog(@"%@", controller);
    NSLog(@"%@", authorization);

    NSLog(@"authorization.credential?%@", authorization.credential);
    POOL_BEGIN();
    if ([authorization.credential isKindOfClass:[ASAuthorizationAppleIDCredential class]]) {
        fireLoginEvent((ASAuthorizationAppleIDCredential *)authorization.credential);
    } else if ([authorization.credential isKindOfClass:[ASPasswordCredential class]]) {
       
    } else {
        
    }
    POOL_END();
}
- (void)authorizationController:(ASAuthorizationController *)controller didCompleteWithError:(NSError *)error  API_AVAILABLE(ios(13.0)){
    
    fireSignInError(error.code, error.localizedDescription);
}

 - (ASPresentationAnchor)presentationAnchorForAuthorizationController:(ASAuthorizationController *)controller  API_AVAILABLE(ios(13.0)){

    NSLog(@"window?%s", __FUNCTION__);
    return [CodenameOne_GLViewController instance].view.window;
}

-(void)getCredentialState:(NSString*)userID{
    if (@available(iOS 13.0, *)) {
        dispatch_async(dispatch_get_main_queue(), ^{
            POOL_BEGIN();
            // A mechanism for generating requests to authenticate users based on their Apple ID.
            ASAuthorizationAppleIDProvider *appleIDProvider = [ASAuthorizationAppleIDProvider new];
            [appleIDProvider getCredentialStateForUserID: userID completion:^(ASAuthorizationAppleIDProviderCredentialState credentialState, NSError *error){
                
                if (error != nil) {
                    fireCheckStateError(error.code, error.localizedDescription);
                    return;
                }
                if (credentialState == ASAuthorizationAppleIDProviderCredentialAuthorized) {
                        fireCheckState(@"Authorized");
                } else if (credentialState == ASAuthorizationAppleIDProviderCredentialNotFound) {
                    
                        fireCheckState(@"NotFound");
                } else if (credentialState == ASAuthorizationAppleIDProviderCredentialRevoked) {
                    
                        fireCheckState(@"Revoked");
                
                }
            }];
            POOL_END();
        });
        
    }
    
}

-(BOOL)isSupported{
    if (@available(iOS 13.0, *)) {
        return YES;
    }
    return NO;
}

- (void)dealloc {

    if (@available(iOS 13.0, *)) {
        [[NSNotificationCenter defaultCenter] removeObserver:self name:ASAuthorizationAppleIDProviderCredentialRevokedNotification object:nil];
    }
}

@end
