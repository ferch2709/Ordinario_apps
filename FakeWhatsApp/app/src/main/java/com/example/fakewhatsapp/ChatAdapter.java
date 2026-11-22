package com.example.fakewhatsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fakewhatsapp.auth.ChatPreview;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatPreview> listaChats;
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatPreview chat);
    }

    public ChatAdapter(List<ChatPreview> listaChats, OnChatClickListener listener) {
        this.listaChats = listaChats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatPreview chat = listaChats.get(position);
        holder.tvName.setText(chat.getOtroNombre());
        holder.tvLastMessage.setText(chat.getUltimoMensaje());

        // Mostrar hora resumida si existe
        if (chat.getFechaUltimoMensaje() != null && chat.getFechaUltimoMensaje().length() >= 16) {
            // Extrae "HH:mm" del formato ISO "2026-11-04T18:30:..."
            String hora = chat.getFechaUltimoMensaje().substring(11, 16);
            holder.tvDate.setText(hora);
        } else {
            holder.tvDate.setText("");
        }

        if (chat.getOtroFoto() != null && !chat.getOtroFoto().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(chat.getOtroFoto())
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return listaChats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName, tvLastMessage, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgChatAvatar);
            tvName = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvDate = itemView.findViewById(R.id.tvChatDate);
        }
    }
}