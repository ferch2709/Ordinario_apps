// src/components/BotonEliminar.js
import React, { useState } from 'react';
import { 
  TouchableOpacity, 
  Text, 
  StyleSheet, 
  Alert, 
  ActivityIndicator 
} from 'react-native';
import { supabase } from '../config/supabase';

export default function BotonEliminar({ id, nombreProducto, onEliminado }) {
  const [eliminando, setEliminando] = useState(false);

  // Alerta de confirmación de seguridad
  const confirmarEliminacion = () => {
    Alert.alert(
      '🗑️ Eliminar Videojuego',
      `¿Estás seguro de que quieres retirar "${nombreProducto}" del catálogo?`,
      [
        { text: 'Cancelar', style: 'cancel' },
        { 
          text: 'Eliminar', 
          style: 'destructive', 
          onPress: ejecutarEliminacion 
        },
      ]
    );
  };

  // Sentencia DELETE en Supabase
  const ejecutarEliminacion = async () => {
    setEliminando(true);
    try {
      const { error } = await supabase
        .from('productos')
        .delete()
        .eq('id', id);

      if (error) throw error;

      Alert.alert('🗑️ Eliminado', `"${nombreProducto}" fue eliminado de la base de datos.`);
      
      // Notificar al componente padre para refrescar la lista
      if (onEliminado) {
        onEliminado();
      }
    } catch (err) {
      Alert.alert('Error al eliminar', err.message);
    } finally {
      setEliminando(false);
    }
  };

  return (
    <TouchableOpacity 
      style={styles.boton} 
      onPress={confirmarEliminacion}
      disabled={eliminando}
    >
      {eliminando ? (
        <ActivityIndicator size="small" color="#F38BA8" />
      ) : (
        <Text style={styles.textoBoton}>🗑️ Eliminar</Text>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  boton: {
    backgroundColor: '#313244',
    paddingVertical: 5,
    paddingHorizontal: 12,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#F38BA8', // Rojo gamer
  },
  textoBoton: {
    color: '#F38BA8',
    fontSize: 12,
    fontWeight: 'bold',
  },
});