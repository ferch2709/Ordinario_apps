const { getDefaultConfig } = require('expo/metro-config');

const config = getDefaultConfig(__dirname);

// Permite empaquetar modelos .tflite dentro de la app
config.resolver.assetExts.push('tflite');

module.exports = config;