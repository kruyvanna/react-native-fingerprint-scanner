const FingerprintScanner = require('./NativeFingerprintScanner').default;

export function setup(): void {
  FingerprintScanner.setup();
}

export function connectDevice(): void {
  FingerprintScanner.connectDevice();
}

export function disconnectDevice(): number {
  return FingerprintScanner.disconnectDevice();
}
