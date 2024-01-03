const FingerprintScanner = require('./NativeFingerprintScanner').default;

export function multiply(a: number, b: number): number {
  return FingerprintScanner.multiply(a, b);
}
