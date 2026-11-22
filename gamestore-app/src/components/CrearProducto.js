// src/components/CrearProducto.js
import React, { useState } from 'react';
import { 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  StyleSheet, 
  Alert, 
  ActivityIndicator 
} from 'react-native';
import { supabase } from '../config/supabase';

export default function CrearProducto({ onProductoCreado }) {
  const [producto, setProducto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');
  const [guardando, setGuardando] = useState(false);

  const guardarJuego = async () => {
    // 1. Validaciones básicas
    if (!producto.trim() || !descripcion.trim() || !precio.trim()) {
      Alert.alert('Atención', 'Por favor completa todos los campos.');
      return;
    }

    const precioNumero = parseFloat(precio);
    if (isNaN(precioNumero) || precioNumero <= 0) {
      Alert.alert('Error', 'El precio debe ser un número válido.');
      return;
    }

    setGuardando(true);

    try {
      // 2. Inserción en la tabla de Supabase (CREATE)
      const { data, error } = await supabase
        .from('productos')
        .insert([
          {
            producto: producto.trim(),
            descripcion: descripcion.trim(),
            precio: precioNumero,
          },
        ])
        .select();

      if (error) {
        throw error;
      }

      Alert.alert('🎮 ¡Éxito!', `El juego "${producto}" fue agregado a la base de datos.`);
      
      // Limpiar el formulario
      setProducto('');
      setDescripcion('');
      setPrecio('');

      // Si le pasamos una función callback para recargar la lista, la ejecuta
      if (onProductoCreado) {
        onProductoCreado();
      }

    } catch (err) {
      Alert.alert('Error al guardar', err.message);
    } finally {
      setGuardando(false);
    }
  };

  return (
    <View style={styles.card}>
      <Text style={styles.subtitulo}>➕ Agregar Nuevo Videojuego</Text>

      <TextInput
        style={styles.input}
        placeholder="Título del juego (ej. God of War Ragnarök)"
        placeholderTextColor="#6C7086"
        value={producto}
        onChangeText={setProducto}
      />

      <TextInput
        style={styles.input}
        placeholder="Descripción (ej. Acción y aventura nórdica PS5)"
        placeholderTextColor="#6C7086"
        value={descripcion}
        onChangeText={setDescripcion}
      />

      <TextInput
        style={styles.input}
        placeholder="Precio (ej. 69.99)"
        placeholderTextColor="#6C7086"
        keyboardType="numeric"
        value={precio}
        onChangeText={setPrecio}
      />

      <TouchableOpacity 
        style={styles.boton} 
        onPress={guardarJuego}
        disabled={guardando}
      >
        {guardando ? (
          <ActivityIndicator color="#11111B" />
        ) : (
          <Text style={styles.textoBoton}>Guardar en Supabase</Text>
        )}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#1E1E2E',
    padding: 18,
    borderRadius: 12,
    width: '100%',
    marginVertical: 12,
    borderWidth: 1,
    borderColor: '#313244',
  },
  subtitulo: {
    color: '#89B4FA',
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 12,
    textAlign: 'center',
  },
  input: {
    backgroundColor: '#313244',
    color: '#CDD6F4',
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderRadius: 8,
    fontSize: 14,
    marginBottom: 10,
  },
  boton: {
    backgroundColor: '#A6E3A1', // Verde gamer
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 4,
  },
  textoBoton: {
    color: '#11111B',
    fontWeight: 'bold',
    fontSize: 15,
  },
});