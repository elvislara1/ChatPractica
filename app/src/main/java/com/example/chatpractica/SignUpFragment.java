package com.example.chatpractica;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatpractica.databinding.FragmentSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

public class SignUpFragment extends Fragment {

    public static class SignUpViewModel extends ViewModel {
        Uri fotoUri;
    }

    private FragmentSignUpBinding binding;
    private FirebaseAuth mAuth;
    private NavController mNav;
    private SignUpViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentSignUpBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        mAuth = FirebaseAuth.getInstance();
        mNav = Navigation.findNavController(view);

        binding.emailSignUp.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();
            String name = binding.name.getText().toString();

            boolean valid = true;

            if (email.isEmpty()) {
                binding.email.setError("Required");
                valid = false;
            }
            if (password.isEmpty()) {
                binding.password.setError("Required");
                valid = false;
            }
            if (name.isEmpty()) {
                binding.name.setError("Required");
                valid = false;
            }
            if (viewModel.fotoUri == null) {
                Toast.makeText(requireContext(), "Seleccione una foto", Toast.LENGTH_SHORT).show();
                valid = false;
            }

            if (valid) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseStorage.getInstance()
                                        .getReference("avatars/" + UUID.randomUUID())
                                        .putFile(viewModel.fotoUri)
                                        .continueWithTask(task2 -> task2.getResult().getStorage().getDownloadUrl())
                                        .addOnSuccessListener(url -> {
                                            mAuth.getCurrentUser().updateProfile(
                                                    new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(name)
                                                            .setPhotoUri(url)
                                                            .build()
                                            );
                                            mNav.navigate(R.id.action_signUpFragment_to_chatFragment);
                                        });
                            } else {
                                Toast.makeText(requireContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        binding.foto.setOnClickListener(v -> {
            galeria.launch("image/*");
        });

        if(viewModel.fotoUri != null) Glide.with(this).load(viewModel.fotoUri).circleCrop().into(binding.foto);
    }

    private final ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        viewModel.fotoUri = uri;
        Glide.with(this).load(uri).circleCrop().into(binding.foto);
    });
}