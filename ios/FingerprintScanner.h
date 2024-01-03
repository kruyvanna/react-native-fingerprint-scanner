
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNFingerprintScannerSpec.h"

@interface FingerprintScanner : NSObject <NativeFingerprintScannerSpec>
#else
#import <React/RCTBridgeModule.h>

@interface FingerprintScanner : NSObject <RCTBridgeModule>
#endif

@end
