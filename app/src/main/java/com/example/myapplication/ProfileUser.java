package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityProfileUserBinding;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class ProfileUser extends AppCompatActivity {

    private ActivityProfileUserBinding binding;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        binding = ActivityProfileUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                                binding.TextImage.setVisibility(View.GONE);
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

        setListeners();
        updateName();
        updatePhoneNumber();
        updateEmail();

    }

    private Bitmap getUserImage(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    private String encodedImage(Bitmap bitmap)
    {
        int previewWith = 450;
        int previewHeight = 400;
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWith, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()==RESULT_OK)
                {
                    if(result.getData() != null)
                    {
                        Uri  imageUri = result.getData().getData();
                        try {

                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.ProfilePhoto.setImageBitmap(bitmap);
                            binding.TextImage.setVisibility(View.GONE);
                            binding.updateImagetext.setVisibility(View.VISIBLE);
                            encodedImage = encodedImage(bitmap);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Firestore operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    public void setListeners()
    {
        binding.ProfilePhoto.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.updateImagetext.setOnClickListener(view -> addImage());
        binding.back.setOnClickListener(v -> onBackPressed());

        binding.image2.setOnClickListener(view -> {
            binding.UserPhone.setVisibility(View.GONE);
            binding.editPhone.setVisibility(View.VISIBLE);
            binding.image2.setVisibility(View.GONE);
            binding.UpdateButton.setVisibility(View.VISIBLE);
            binding.editPhone.setText(binding.UserPhone.getText().toString());
            binding.editPhone.requestFocus();
        });

        binding.editPhone.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String newPhoneNumber = binding.editPhone.getText().toString();
                savePhoneNumber(newPhoneNumber);
                binding.editPhone.setVisibility(View.GONE);
                binding.UserPhone.setVisibility(View.VISIBLE);

                // Show the edit icon (image2) and hide the UpdateButton
                binding.image2.setVisibility(View.VISIBLE);
                binding.UpdateButton.setVisibility(View.GONE);
                return true;
            }
            return false;
        });
        binding.UpdateButton.setOnClickListener(view -> {
            String newPhoneNumber = binding.editPhone.getText().toString();
            savePhoneNumber(newPhoneNumber);

            // Hide the EditText and show the TextView
            binding.UserPhone.setText(newPhoneNumber);
            binding.editPhone.setVisibility(View.GONE);
            binding.UserPhone.setVisibility(View.VISIBLE);

            // Show the edit icon (image2) and hide the UpdateButton
            binding.image2.setVisibility(View.VISIBLE);
            binding.UpdateButton.setVisibility(View.GONE);
        });

        binding.ProfilePhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Users users = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);


                if (users.image != null) {


                    if (users != null) {
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection(Constants.KEY_COLLECTION_USERS)
                                .document(users.id)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String ProfilePhoto = documentSnapshot.getString(Constants.KEY_IMAGE);
                                        showFullImage(ProfilePhoto);
                                    } else {

                                    }
                                })
                                .addOnFailureListener(exception -> {

                                });
                    } else {
                        binding.ProfilePhoto.setImageResource(R.drawable.user);

                    }


                }
                return true; // Consume the long click event
            }
        });
    }

    private void addImage() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_IMAGE, encodedImage);

        Users users = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);


        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(users.id)
                .update(Constants.KEY_IMAGE, encodedImage)
                .addOnSuccessListener(aVoid -> {


                    byte[] imageBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap newBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    Bitmap scaledBitmap = scaleBitmap(newBitmap, binding.ProfilePhoto.getWidth(), binding.ProfilePhoto.getHeight());
                    binding.ProfilePhoto.setImageBitmap(scaledBitmap);
                    binding.updateImagetext.setVisibility(View.GONE);


                    Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(this, "Failed to update profile picture: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });


    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleRatio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        int newWidth = Math.round(width * scaleRatio);
        int newHeight = Math.round(height * scaleRatio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void savePhoneNumber(String newPhoneNumber) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_PHONENUMBER, newPhoneNumber);

        Users users = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);

        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(users.id)
                .update(Constants.KEY_PHONENUMBER, newPhoneNumber)
                .addOnSuccessListener(aVoid -> {
                    // Update the UI with the new phone number
                    binding.UserPhone.setText(newPhoneNumber);

                    // Hide the EditText and show the TextView
                    binding.editPhone.setVisibility(View.GONE);
                    binding.UserPhone.setVisibility(View.VISIBLE);


                    Toast.makeText(this, "Phone number updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(this, "Failed to update phone number: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void updateName()
    {
        Users users = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);

        if (users != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection(Constants.KEY_COLLECTION_USERS)
                    .document(users.id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String Name = documentSnapshot.getString(Constants.KEY_NAME);
                            String Surname = documentSnapshot.getString(Constants.KEY_SURNAME);
                            binding.UserName.setText(Name+" "+Surname);
                        } else {
                            binding.UserName.setText("Name not found");
                        }
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(this, "Failed to fetch Name: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            binding.UserName.setText("Name not found");
        }
    }

    public void updateEmail()
    {
        Users users = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);

        if (users != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection(Constants.KEY_COLLECTION_USERS)
                    .document(users.id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String Email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            binding.TextEmail.setText(Email);
                        } else {
                            binding.TextEmail.setText("Email not found");
                        }
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(this, "Failed to fetch Email: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            binding.TextEmail.setText("Email not found");
        }
    }

    public void updatePhoneNumber()
    {
        Users users = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);

        if (users != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection(Constants.KEY_COLLECTION_USERS)
                    .document(users.id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String phoneNumber = documentSnapshot.getString(Constants.KEY_PHONENUMBER);
                            binding.UserPhone.setText(phoneNumber);
                        } else {
                            binding.UserPhone.setText("PhoneNumber not found");
                        }
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(this, "Failed to fetch phone number: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            binding.UserPhone.setText("PhoneNumber not found");
        }
    }

    private void showFullImage(String encodedImage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_full_image, null);
        builder.setView(dialogView);

        ImageView fullImageView = dialogView.findViewById(R.id.fullImageView);
        Button closeButton = dialogView.findViewById(R.id.closeButton);

        if (encodedImage != null && !encodedImage.isEmpty()) {
            Bitmap fullImageBitmap = getUserImage(encodedImage);
            fullImageView.setImageBitmap(fullImageBitmap);
        } else {
            fullImageView.setImageResource(R.drawable.user);
        }

        AlertDialog dialog = builder.create();

        // Set the dialog window dimensions to be half of the screen
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = getWindowManager().getDefaultDisplay().getHeight() / 2;
        dialog.getWindow().setAttributes(layoutParams);

        dialog.show();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }



}