import * as React from 'react';

import { StyleSheet, View, Text, Button, Image } from 'react-native';
import {
  setup,
  connectDevice,
  disconnectDevice,
} from 'react-native-zk-fingerprint-scanner';
import { DeviceEventEmitter } from 'react-native';

export default function App() {
  const [image, setImage] = React.useState<string | undefined>(undefined);
  const [isConnected, setIsConnected] = React.useState<boolean>(false);

  React.useEffect(() => {
    DeviceEventEmitter.addListener('onDeviceConnected', (e) => {
      console.log('onDeviceConnected', e);
      setIsConnected(true);
    });

    DeviceEventEmitter.addListener('onDeviceDisconnected', (e) => {
      console.log('onDeviceDisconnected', e);
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
