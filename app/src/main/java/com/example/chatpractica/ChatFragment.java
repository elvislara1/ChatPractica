package com.example.chatpractica;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.chatpractica.databinding.FragmentChatBinding;
import com.example.chatpractica.databinding.ViewholderChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private FirebaseFirestore mDb;
    private FirebaseStorage storage;
    private List<Mensaje> mensajes = new ArrayList<>();
    private FirebaseUser user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentChatBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDb = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        binding.enviar.setOnClickListener(v -> {
            String mensaje = binding.mensaje.getText().toString();
            String fecha = LocalDateTime.now().toString();

            mDb.collection("mensajes")
                    .add(new Mensaje(user.getEmail(), user.getDisplayName(), user.getPhotoUrl().toString(), mensaje, fecha, null));
            binding.mensaje.setText("");
        });

        binding.adjuntar.setOnClickListener(v -> {
            galeria.launch("image/*");
        });

        ChatAdapter chatAdapter = new ChatAdapter();
        binding.recyclerView.setAdapter(chatAdapter);

        // collection ~~~ tabla
        // document ~~~ fila

        mDb.collection("mensajes")
                .orderBy("fecha")
                .addSnapshotListener((value, error) -> {
                    for (QueryDocumentSnapshot m: value){

                        String email = m.getString("autorEmail");
                        String nombre = m.getString("autorNombre");
                        String fecha = m.getString("fecha");
                        String texto = m.getString("mensaje");
                        String foto = m.getString("autorFoto");
                        String adjunto = m.getString("adjunto");

                        Mensaje mensaje = new Mensaje(email, nombre, foto, texto, fecha, adjunto);
                        mensajes.add(mensaje);
                    }
                    chatAdapter.notifyDataSetChanged();
                    binding.recyclerView.scrollToPosition(mensajes.size() - 1);
                });
    }

    class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder>{

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ChatViewHolder(ViewholderChatBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            Mensaje mensaje = mensajes.get(position);

            if(mensaje.autorEmail != null && mensaje.autorEmail.equals(user.getEmail())){
                holder.binding.todo.setGravity(Gravity.END);
            } else {
                holder.binding.todo.setGravity(Gravity.START);
            }
            holder.binding.autor.setText(mensaje.autorNombre);

            if(mensaje.adjunto == null) {
                holder.binding.adjunto.setVisibility(View.GONE);
                holder.binding.mensaje.setVisibility(View.VISIBLE);

                holder.binding.mensaje.setText(mensaje.mensaje);
            } else {
                holder.binding.adjunto.setVisibility(View.VISIBLE);
                holder.binding.mensaje.setVisibility(View.GONE);

                Glide.with(requireView()).load(mensaje.adjunto).into(holder.binding.adjunto);
            }

            holder.binding.fecha.setText(mensaje.fecha);
            Glide.with(requireView()).load(mensaje.autorFoto).into(holder.binding.foto);
        }

        @Override
        public int getItemCount() {
            return mensajes.size();
        }
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder{
        ViewholderChatBinding binding;
        public ChatViewHolder(@NonNull ViewholderChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private final ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        storage.getReference("adjuntos/"+ UUID.randomUUID())
                .putFile(uri)
                .continueWithTask(task -> task.getResult().getStorage().getDownloadUrl())
                .addOnSuccessListener(url -> {
                    String fecha = LocalDateTime.now().toString();

                    mDb.collection("mensajes")
                            .add(new Mensaje(user.getEmail(), user.getDisplayName(), user.getPhotoUrl().toString(), null, fecha, url.toString()));

                });
    });
}