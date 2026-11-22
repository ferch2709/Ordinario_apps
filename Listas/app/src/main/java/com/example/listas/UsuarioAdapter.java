package com.example.listas; // Cambia esto por el nombre de tu paquete

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<Usuario> listaUsuarios;

    public UsuarioAdapter(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    // ==========================================
    // 1. DEFINICIÓN DEL HOLDER (ViewHolder)
    // ==========================================
    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre;
        private TextView tvCorreo;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCorreo = itemView.findViewById(R.id.tvCorreo);
        }

        public void bind(Usuario usuario) {
            tvNombre.setText(usuario.getNombre());
            tvCorreo.setText(usuario.getCorreo());
        }
    }

    // ==========================================
    // 2. MÉTODOS DEL ADAPTER
    // ==========================================
    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        holder.bind(listaUsuarios.get(position));
    }

    @Override
    public int getItemCount() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
    }
}