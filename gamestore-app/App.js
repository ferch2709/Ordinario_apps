// App.js
import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View, SafeAreaView } from 'react-native';
import CrearProducto from './src/components/CrearProducto';
import ListaProductos from './src/components/ListaProductos';

export default function App() {
  // Estado para coordinar la actualización entre componentes
  const [recargar, setRecargar] = useState(false);

  const refrescarLista = () => {
    setRecargar((prev) => !prev);
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.appTitle}>🎮 Pixel Vault Store 🕹️</Text>

        {/* Componente CREATE (Etapa d) */}
        <CrearProducto onProductoCreado={refrescarLista} />

        {/* Componente READ con FlatList (Etapa e) */}
        <ListaProductos recargar={recargar} />
      </View>
      <StatusBar style="light" />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#11111B',
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 45,
  },
  appTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#CBA6F7',
    textAlign: 'center',
    marginBottom: 8,
  },
});