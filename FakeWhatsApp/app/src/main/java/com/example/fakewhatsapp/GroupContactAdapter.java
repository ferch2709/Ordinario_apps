package com.example.fakewhatsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fakewhatsapp.auth.Perfil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupContactAdapter extends RecyclerView.Adapter<GroupContactAdapter.ViewHolder> {

    private final List<Perfil> listaContactos;
    private final List<String> seleccionados = new ArrayList<>();

    public GroupContactAdapter(List<Perfil> listaContactos) {
        this.listaContactos = listaContactos;
    }

    public List<String> getSeleccionados() {
        return seleccionados;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Perfil p = listaContactos.get(position);
        holder.tvName.setText(p.getNombre());
        if (p.getFotoUrl() != null) {
            Glide.with(holder.itemView.getContext()).load(p.getFotoUrl()).into(holder.imgAvatar);
        }

        holder.cbSelect.setChecked(seleccionados.contains(p.getId()));

        holder.itemView.setOnClickListener(v -> {
            if (seleccionados.contains(p.getId())) {
                seleccionados.remove(p.getId());
                holder.cbSelect.setChecked(false);
            } else {
                seleccionados.add(p.getId());
                holder.cbSelect.setChecked(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName;
        CheckBox cbSelect;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgGroupAvatar);
            tvName = itemView.findViewById(R.id.tvGroupContactName);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}