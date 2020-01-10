#import <Foundation/Foundation.h>
#import <AuthenticationServices/AuthenticationServices.h>

@interface com_codename1_auth_apple_AppleSignInNativeImpl : NSObject<ASAuthorizationControllerDelegate,ASAuthorizationControllerPresentationContextProviding> {
}

-(void)doLogin;
-(void)initializeStateChangeNotifications;
-(void)getCredentialState:(NSString*)param;
-(BOOL)isSupported;
@end
