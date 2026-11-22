// src/components/TestConexion.js
import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import { supabase } from '../config/supabase';

export default function TestConexion() {
  const [estado, setEstado] = useState('Conectando a Supabase...');
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    probarConexion();
  }, []);

  const probarConexion = async () => {
    try {
      // Hacemos una consulta rápida a la tabla productos
      const { data, error } = await supabase.from('productos').select('id').limit(1);

      if (error) {
        setEstado('❌ Error al conectar: ' + error.message);
      } else {
        setEstado('✅ ¡Conexión exitosa con la GameStore en Supabase!');
      }
    } catch (err) {
      setEstado('❌ Error inesperado: ' + err.message);
    } finally {
      setCargando(false);
    }
  };

  return (
    <View style={styles.card}>
      <Text style={styles.titulo}>Estado de Supabase:</Text>
      {cargando ? (
        <ActivityIndicator color="#6200EE" size="small" />
      ) : (
        <Text style={styles.resultado}>{estado}</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    padding: 16,
    backgroundColor: '#1E1E2E',
    borderRadius: 8,
    marginVertical: 10,
    alignItems: 'center',
    width: '90%',
  },
  titulo: {
    color: '#CDD6F4',
    fontSize: 14,
    marginBottom: 6,
  },
  resultado: {
    color: '#A6E3A1',
    fontWeight: 'bold',
    textAlign: 'center',
  },
});