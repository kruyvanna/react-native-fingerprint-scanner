# react-native-fingerprint-scanner

Des

## Installation

```sh
npm install react-native-fingerprint-scanner
```

## Usage

```js
import * as React from 'react';
import { StyleSheet, View, Text, Button, Image } from 'react-native';
import {
  setup,
  connectDevice,
  disconnectDevice,
} from 'react-native-fingerprint-scanner';
import { DeviceEventEmitter } from 'react-native';

export default function App() {
  const [image, setImage] = (React.useState < string) | (undefined > undefined);
  const [isConnected, setIsConnected] = React.useState < boolean > false;

  React.useEffect(() => {
    DeviceEventEmitter.addListener('onDeviceConnected', (e) => {
      setIsConnected(true);
    });

    DeviceEventEmitter.addListener('onDeviceDisconnected', (e) => {
      setIsConnected(false);
    });

    DeviceEventEmitter.addListener('onGotImage', (e) => {
      setImage(e);
    });
  }, []);

  return (
    <View style={styles.container}>
      <Image
        source={{ uri: `data:image/png;base64,${image}` }}
        style={{ width: 100, height: 100, backgroundColor: 'blue' }}
      />
      <Text>Connected: {isConnected ? 'Yes' : 'No'}</Text>

      <Button
        title="Setup"
        onPress={() => {
          setup();
        }}
      ></Button>
      <Button
        title="Connect device"
        onPress={async () => {
          connectDevice();
        }}
      ></Button>
      <Button
        title="Disconnect"
        onPress={async () => {
          disconnectDevice();
        }}
      ></Button>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 20,
  },
});
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
