import React, { useState, useRef } from 'react';
import { StyleSheet, Text, View, TouchableOpacity, Alert, ActivityIndicator } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { Asset, usePermissions as useMediaPermissions } from 'expo-media-library';
import FaceDetection from '@react-native-ml-kit/face-detection';

export default function App() {
  const [cameraPermission, requestCameraPermission] = useCameraPermissions();
  const [mediaPermission, requestMediaPermission] = useMediaPermissions();
  const cameraRef = useRef(null);
  const [facing, setFacing] = useState('front');
  const [processing, setProcessing] = useState(false);
  const [statusMessage, setStatusMessage] = useState('Listo');

  if (!cameraPermission || !mediaPermission) {
    return <View style={styles.container} />;
  }

  if (!cameraPermission.granted || !mediaPermission.granted) {
    return (
      <View style={styles.centerContainer}>
        <Text style={styles.permissionText}>Se requieren permisos de Cámara y Galería.</Text>
        <TouchableOpacity
          style={styles.primaryButton}
          onPress={async () => {
            await requestCameraPermission();
            await requestMediaPermission();
          }}
        >
          <Text style={styles.buttonText}>Conceder Permisos</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const takePhotoAndDetectFace = async () => {
    if (!cameraRef.current || processing) return;

    try {
      setProcessing(true);
      setStatusMessage('Capturando...');

      // 1. Tomar foto
      const photo = await cameraRef.current.takePictureAsync({
        quality: 0.8,
        skipProcessing: false,
      });

      if (!photo?.uri) throw new Error('No se pudo capturar la imagen.');

      // 2. Guardar en Galería usando la nueva API
      setStatusMessage('Guardando en galería...');
      await Asset.create(photo.uri);

      // 3. IA local offline (Google ML Kit)
      setStatusMessage('Detectando rostro...');
      const faces = await FaceDetection.detect(photo.uri, {
        landmarkMode: 'all',
        classificationMode: 'all',
      });

      setProcessing(false);
      setStatusMessage('Listo');

      if (faces && faces.length > 0) {
        Alert.alert('Éxito', `¡Rostro detectado y foto guardada!\nCaras encontradas: ${faces.length}`);
      } else {
        Alert.alert('Aviso', 'Foto guardada, pero no se detectaron rostros claros.');
      }
    } catch (error) {
      setProcessing(false);
      setStatusMessage('Listo');
      Alert.alert('Error', error.message);
    }
  };

  return (
    <View style={styles.container}>
      <CameraView style={styles.camera} facing={facing} ref={cameraRef}>
        <View style={styles.overlay}>
          <View style={styles.faceBoxGuide} />

          <View style={styles.statusBadge}>
            {processing && <ActivityIndicator size="small" color="#fff" style={{ marginRight: 8 }} />}
            <Text style={styles.statusText}>{statusMessage}</Text>
          </View>

          <View style={styles.controlsRow}>
            <TouchableOpacity
              style={styles.actionBtn}
              onPress={() => setFacing((f) => (f === 'front' ? 'back' : 'front'))}
              disabled={processing}
            >
              <Text style={styles.actionBtnText}>Voltear</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.captureCircle, processing && { opacity: 0.5 }]}
              onPress={takePhotoAndDetectFace}
              disabled={processing}
            >
              <View style={styles.innerCaptureCircle} />
            </TouchableOpacity>
          </View>
        </View>
      </CameraView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#000' },
  centerContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24, backgroundColor: '#121212' },
  permissionText: { color: '#fff', fontSize: 16, textAlign: 'center', marginBottom: 20 },
  primaryButton: { backgroundColor: '#007AFF', paddingHorizontal: 24, paddingVertical: 12, borderRadius: 8 },
  buttonText: { color: '#fff', fontWeight: 'bold' },
  camera: { flex: 1 },
  overlay: { flex: 1, justifyContent: 'space-between', alignItems: 'center', paddingVertical: 50 },
  faceBoxGuide: { width: 250, height: 330, borderWidth: 2, borderColor: 'rgba(0, 255, 120, 0.7)', borderRadius: 125, borderStyle: 'dashed', marginTop: 30 },
  statusBadge: { flexDirection: 'row', alignItems: 'center', backgroundColor: 'rgba(0,0,0,0.6)', paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20 },
  statusText: { color: '#fff', fontSize: 14 },
  controlsRow: { flexDirection: 'row', width: '100%', justifyContent: 'space-around', alignItems: 'center' },
  actionBtn: { backgroundColor: 'rgba(255,255,255,0.2)', paddingVertical: 12, paddingHorizontal: 20, borderRadius: 25 },
  actionBtnText: { color: '#fff', fontWeight: '600' },
  captureCircle: { width: 76, height: 76, borderRadius: 38, borderWidth: 4, borderColor: '#fff', justifyContent: 'center', alignItems: 'center' },
  innerCaptureCircle: { width: 58, height: 58, borderRadius: 29, backgroundColor: '#fff' },
});