package com.example.myapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.UserRecentchatViewBinding;
import com.example.myapplication.listeners.ConversationListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.Users;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;


    public ConversationAdapter(List<ChatMessage> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }


    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                UserRecentchatViewBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));


    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    class ConversionViewHolder extends RecyclerView.ViewHolder {
        UserRecentchatViewBinding binding;


        ConversionViewHolder(UserRecentchatViewBinding userRecentchatViewBinding) {
            super(userRecentchatViewBinding.getRoot());
            binding = userRecentchatViewBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textName.setText(chatMessage.conversationName);
            binding.recentMessage.setText(chatMessage.message);
            binding.time.setText(chatMessage.dateTime);
            binding.imageView3.setImageBitmap(getConversionImage(chatMessage.conversationImage));
            ChatMessage chatMessage1 = new ChatMessage();
            if (chatMessage1.isRead) {
                binding.imageView4.setVisibility(View.GONE);
                binding.numbermessage.setVisibility(View.GONE);
            } else {
                binding.imageView4.setVisibility(View.VISIBLE);
                binding.numbermessage.setVisibility(View.VISIBLE);
            }

            binding.getRoot().setOnClickListener(view -> {
                chatMessage1.isRead = true;
                Users user = new Users();
                binding.imageView4.setVisibility(View.GONE);
                binding.numbermessage.setVisibility(View.GONE);
                user.id = chatMessage.conversationId;
                user.Name = chatMessage.conversationName;
                user.image = chatMessage.conversationImage;



                conversationListener.onConversionListener(user);
                    });

        }
    }

    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
