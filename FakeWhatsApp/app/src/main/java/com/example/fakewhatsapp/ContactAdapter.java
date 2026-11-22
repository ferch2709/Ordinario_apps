package com.example.fakewhatsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fakewhatsapp.auth.Perfil;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private final List<Perfil> listaContactos;
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(Perfil perfil);
    }

    public ContactAdapter(List<Perfil> listaContactos, OnContactClickListener listener) {
        this.listaContactos = listaContactos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Perfil contacto = listaContactos.get(position);
        holder.tvName.setText(contacto.getNombre());
        holder.tvStatus.setText(contacto.getEstadoInfo());

        if (contacto.getFotoUrl() != null && !contacto.getFotoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(contacto.getFotoUrl())
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        // Seleccionar destinatario
        holder.itemView.setOnClickListener(v -> listener.onContactClick(contacto));
    }

    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgContactAvatar);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvStatus = itemView.findViewById(R.id.tvContactStatus);
        }
    }
}