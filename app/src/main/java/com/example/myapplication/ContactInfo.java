package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityContactInfoBinding;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

public class ContactInfo extends AppCompatActivity {

    private TextView textName, textPhone, textEmail;

    private ActivityContactInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        binding = ActivityContactInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setData();


    }

    public void setData() {
        Users users = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);

        if (users.image != null)
        {
            if (users != null) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection(Constants.KEY_COLLECTION_USERS)
                        .document(users.id)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String ProfilePhoto = documentSnapshot.getString(Constants.KEY_IMAGE);
                                binding.ProfilePhoto.setImageBitmap(getUserImage(ProfilePhoto));
                                String Email = documentSnapshot.getString(Constants.KEY_RECEIVER_Email);
                                binding.TextEmail.setText(Email);
                                binding.TextName.setText(users.Name);
                                binding.TextPhone.setText(users.PhoneNumber);

                            } else {
                                binding.ProfilePhoto.setImageResource(R.drawable.user);

                            }
                        })
                        .addOnFailureListener(exception -> {
                            Toast.makeText(this, "Failed to fetch Image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                binding.ProfilePhoto.setImageResource(R.drawable.user);


            }

        }

    }

    private Bitmap getUserImage(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
