// src/config/supabase.js
import 'react-native-url-polyfill/auto';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { createClient } from '@supabase/supabase-js';

// 1. Datos de conexión de tu proyecto de Supabase
const SUPABASE_URL = 'https://ggphbsatulhnjfxgqvrr.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdncGhic2F0dWxobmpmeGdxdnJyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTMwNjY3NzMsImV4cCI6MjEwODY0Mjc3M30.RPQSTNbUAkGAcnzYzZMU-vxXgciuJjzqWn3Alf4LAHo';

// 2. Creación e inicialización del cliente
export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    storage: AsyncStorage,
    autoRefreshToken: true,
    persistSession: true,
    detectSessionInUrl: false,
  },
});