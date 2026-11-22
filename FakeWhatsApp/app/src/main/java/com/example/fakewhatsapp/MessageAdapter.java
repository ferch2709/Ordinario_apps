package com.example.fakewhatsapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fakewhatsapp.auth.Mensaje;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(Mensaje mensaje, int position);
    }

    private List<Mensaje> listaMensajes;
    private final String miUsuarioId;
    private final OnMessageLongClickListener longClickListener;

    public MessageAdapter(List<Mensaje> listaMensajes, String miUsuarioId, OnMessageLongClickListener longClickListener) {
        this.listaMensajes = listaMensajes;
        this.miUsuarioId = miUsuarioId;
        this.longClickListener = longClickListener;
    }

    public void actualizarLista(List<Mensaje> nuevaLista) {
        this.listaMensajes = nuevaLista;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return listaMensajes.get(position).getEmisorId().equals(miUsuarioId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje mensaje = listaMensajes.get(position);
        String hora = (mensaje.getCreadoEn() != null && mensaje.getCreadoEn().length() >= 16)
                ? mensaje.getCreadoEn().substring(11, 16) : "";

        ImageView ivMedia = (holder instanceof SentViewHolder) ? ((SentViewHolder) holder).ivMedia : ((ReceivedViewHolder) holder).ivMedia;
        TextView tvContent = (holder instanceof SentViewHolder) ? ((SentViewHolder) holder).tvContent : ((ReceivedViewHolder) holder).tvContent;
        TextView tvTime = (holder instanceof SentViewHolder) ? ((SentViewHolder) holder).tvTime : ((ReceivedViewHolder) holder).tvTime;

        tvTime.setText(hora);

        if ("imagen".equals(mensaje.getTipoMensaje()) && mensaje.getMediaUrl() != null && !mensaje.getMediaUrl().isEmpty()) {
            ivMedia.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext()).load(mensaje.getMediaUrl()).into(ivMedia);
        } else {
            ivMedia.setVisibility(View.GONE);
        }

        if (mensaje.getContenido() != null && !mensaje.getContenido().isEmpty()) {
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(mensaje.getContenido());
        } else {
            tvContent.setVisibility(View.GONE);
        }

        if (holder instanceof SentViewHolder) {
            SentViewHolder sent = (SentViewHolder) holder;
            String estado = mensaje.getEstado() != null ? mensaje.getEstado() : "enviado";
            if ("leido".equals(estado)) {
                sent.tvStatusCheck.setText("✓✓");
                sent.tvStatusCheck.setTextColor(Color.parseColor("#53BDEB"));
            } else if ("recibido".equals(estado) || "entregado".equals(estado)) {
                sent.tvStatusCheck.setText("✓✓");
                sent.tvStatusCheck.setTextColor(Color.parseColor("#8696A0"));
            } else {
                sent.tvStatusCheck.setText("✓");
                sent.tvStatusCheck.setTextColor(Color.parseColor("#8696A0"));
            }
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onMessageLongClick(mensaje, holder.getAdapterPosition());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaMensajes.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedia;
        TextView tvContent, tvTime, tvStatusCheck;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvStatusCheck = itemView.findViewById(R.id.tvStatusCheck);
        }
    }

    public static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedia;
        TextView tvContent, tvTime;
        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
        }
    }
}