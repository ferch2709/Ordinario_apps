// src/components/ListaProductos.js
import React, { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  FlatList, 
  TouchableOpacity,
  StyleSheet, 
  ActivityIndicator, 
  RefreshControl 
} from 'react-native';
import { supabase } from '../config/supabase';
import ModalEditarProducto from './ModalEditarProducto';
import BotonEliminar from './BotonEliminar';

export default function ListaProductos({ recargar }) {
  const [productos, setProductos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [refrescando, setRefrescando] = useState(false);

  // Estados para el modal de edición
  const [juegoSeleccionado, setJuegoSeleccionado] = useState(null);
  const [modalVisible, setModalVisible] = useState(false);

  const obtenerProductos = async () => {
    try {
      const { data, error } = await supabase
        .from('productos')
        .select('*')
        .order('id', { ascending: false });

      if (error) {
        console.error('Error al consultar:', error.message);
      } else {
        setProductos(data || []);
      }
    } catch (err) {
      console.error('Error:', err.message);
    } finally {
      setCargando(false);
      setRefrescando(false);
    }
  };

  useEffect(() => {
    obtenerProductos();
  }, [recargar]);

  const alRefrescar = () => {
    setRefrescando(true);
    obtenerProductos();
  };

  const abrirModalEditar = (item) => {
    setJuegoSeleccionado(item);
    setModalVisible(true);
  };

  const renderItem = ({ item }) => (
    <View style={styles.card}>
      <View style={styles.headerCard}>
        <Text style={styles.tituloJuego}>🎮 {item.producto}</Text>
        <Text style={styles.precio}>${parseFloat(item.precio || 0).toFixed(2)}</Text>
      </View>
      
      <Text style={styles.descripcion}>{item.descripcion}</Text>

      <View style={styles.footerCard}>
        <Text style={styles.idTag}>ID: #{item.id}</Text>
        
        <View style={styles.grupoBotones}>
          {/* Botón Editar (UPDATE) */}
          <TouchableOpacity 
            style={styles.botonEditar} 
            onPress={() => abrirModalEditar(item)}
          >
            <Text style={styles.textoBotonEditar}>✏️ Editar</Text>
          </TouchableOpacity>

          {/* Componente Eliminar (DELETE) */}
          <BotonEliminar
            id={item.id}
            nombreProducto={item.producto}
            onEliminado={obtenerProductos}
          />
        </View>
      </View>
    </View>
  );

  if (cargando) {
    return (
      <View style={styles.centro}>
        <ActivityIndicator size="large" color="#CBA6F7" />
        <Text style={styles.textoCargando}>Cargando catálogo gamer...</Text>
      </View>
    );
  }

  return (
    <View style={styles.contenedor}>
      <Text style={styles.seccionTitulo}>🕹️ Catálogo Disponible</Text>
      
      <FlatList
        data={productos}
        keyExtractor={(item) => String(item.id)}
        renderItem={renderItem}
        refreshControl={
          <RefreshControl 
            refreshing={refrescando} 
            onRefresh={alRefrescar} 
            tintColor="#CBA6F7" 
          />
        }
        ListEmptyComponent={
          <Text style={styles.vacio}>No hay videojuegos registrados aún.</Text>
        }
      />

      {/* Modal para Editar */}
      <ModalEditarProducto
        visible={modalVisible}
        juego={juegoSeleccionado}
        onCerrar={() => setModalVisible(false)}
        onProductoActualizado={obtenerProductos}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  contenedor: {
    flex: 1,
    width: '100%',
    marginTop: 10,
  },
  seccionTitulo: {
    color: '#F5C2E7',
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  card: {
    backgroundColor: '#1E1E2E',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#313244',
  },
  headerCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  tituloJuego: {
    color: '#CDD6F4',
    fontSize: 16,
    fontWeight: 'bold',
    flex: 1,
  },
  precio: {
    color: '#A6E3A1',
    fontSize: 16,
    fontWeight: 'bold',
    marginLeft: 10,
  },
  descripcion: {
    color: '#A6ADC8',
    fontSize: 13,
    lineHeight: 18,
  },
  footerCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 12,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#313244',
  },
  idTag: {
    color: '#585B70',
    fontSize: 12,
  },
  grupoBotones: {
    flexDirection: 'row',
    gap: 8,
  },
  botonEditar: {
    backgroundColor: '#45475A',
    paddingVertical: 5,
    paddingHorizontal: 12,
    borderRadius: 6,
  },
  textoBotonEditar: {
    color: '#F9E2AF',
    fontSize: 12,
    fontWeight: 'bold',
  },
  centro: {
    padding: 20,
    alignItems: 'center',
  },
  textoCargando: {
    color: '#A6ADC8',
    marginTop: 10,
  },
  vacio: {
    color: '#6C7086',
    textAlign: 'center',
    marginVertical: 20,
    fontStyle: 'italic',
  },
});