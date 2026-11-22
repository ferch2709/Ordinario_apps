// src/components/ModalEditarProducto.js
import React, { useState, useEffect } from 'react';
import { 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  Modal, 
  StyleSheet, 
  Alert, 
  ActivityIndicator 
} from 'react-native';
import { supabase } from '../config/supabase';

export default function ModalEditarProducto({ visible, juego, onCerrar, onProductoActualizado }) {
  const [producto, setProducto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');
  const [actualizando, setActualizando] = useState(false);

  // Rellenar los campos con los datos actuales del juego seleccionado
  useEffect(() => {
    if (juego) {
      setProducto(juego.producto || '');
      setDescripcion(juego.descripcion || '');
      setPrecio(juego.precio ? juego.precio.toString() : '');
    }
  }, [juego]);

  // Función UPDATE a Supabase
  const actualizarJuego = async () => {
    if (!producto.trim() || !descripcion.trim() || !precio.trim()) {
      Alert.alert('Atención', 'Todos los campos son obligatorios.');
      return;
    }

    const precioNumero = parseFloat(precio);
    if (isNaN(precioNumero) || precioNumero <= 0) {
      Alert.alert('Error', 'Ingresa un precio válido.');
      return;
    }

    setActualizando(true);

    try {
      // Sentencia UPDATE de Supabase filtrando por ID
      const { error } = await supabase
        .from('productos')
        .update({
          producto: producto.trim(),
          descripcion: descripcion.trim(),
          precio: precioNumero,
        })
        .eq('id', juego.id);

      if (error) throw error;

      Alert.alert('✅ Actualizado', `"${producto}" ha sido actualizado correctamente.`);
      onProductoActualizado(); // Recarga la lista
      onCerrar();              // Cierra la ventana
    } catch (err) {
      Alert.alert('Error al actualizar', err.message);
    } finally {
      setActualizando(false);
    }
  };

  if (!juego) return null;

  return (
    <Modal visible={visible} transparent animationType="slide">
      <View style={styles.fondoModal}>
        <View style={styles.contenidoModal}>
          <Text style={styles.tituloModal}>✏️ Editar Videojuego</Text>
          <Text style={styles.subtituloModal}>ID: #{juego.id}</Text>

          <TextInput
            style={styles.input}
            placeholder="Título del juego"
            placeholderTextColor="#6C7086"
            value={producto}
            onChangeText={setProducto}
          />

          <TextInput
            style={styles.input}
            placeholder="Descripción"
            placeholderTextColor="#6C7086"
            value={descripcion}
            onChangeText={setDescripcion}
          />

          <TextInput
            style={styles.input}
            placeholder="Precio"
            placeholderTextColor="#6C7086"
            keyboardType="numeric"
            value={precio}
            onChangeText={setPrecio}
          />

          <View style={styles.filaBotones}>
            <TouchableOpacity 
              style={[styles.boton, styles.botonCancelar]} 
              onPress={onCerrar}
              disabled={actualizando}
            >
              <Text style={styles.textoBoton}>Cancelar</Text>
            </TouchableOpacity>

            <TouchableOpacity 
              style={[styles.boton, styles.botonGuardar]} 
              onPress={actualizarJuego}
              disabled={actualizando}
            >
              {actualizando ? (
                <ActivityIndicator color="#11111B" />
              ) : (
                <Text style={styles.textoBotonGuardar}>Guardar</Text>
              )}
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  fondoModal: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  contenidoModal: {
    backgroundColor: '#1E1E2E',
    borderRadius: 14,
    padding: 22,
    width: '100%',
    borderWidth: 1,
    borderColor: '#45475A',
  },
  tituloModal: {
    color: '#F9E2AF',
    fontSize: 18,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  subtituloModal: {
    color: '#6C7086',
    fontSize: 12,
    textAlign: 'center',
    marginBottom: 16,
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
  filaBotones: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 10,
    gap: 10,
  },
  boton: {
    flex: 1,
    paddingVertical: 11,
    borderRadius: 8,
    alignItems: 'center',
  },
  botonCancelar: {
    backgroundColor: '#313244',
  },
  botonGuardar: {
    backgroundColor: '#F9E2AF', // Amarillo gamer
  },
  textoBoton: {
    color: '#CDD6F4',
    fontWeight: 'bold',
  },
  textoBotonGuardar: {
    color: '#11111B',
    fontWeight: 'bold',
  },
});