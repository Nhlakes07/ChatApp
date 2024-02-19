package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.MessageOptionsDialog;
import com.example.myapplication.UserPage;
import com.example.myapplication.databinding.ContainerChat1Binding;
import com.example.myapplication.databinding.ContainerChatBinding;
import com.example.myapplication.databinding.Containerchatimage1Binding;
import com.example.myapplication.databinding.ContainerchatimageBinding;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.utilities.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnCreateContextMenuListener {

    private final List<ChatMessage> chatMessages;
    private final String senderId;

    private Context context;

    private final Bitmap receiverProfileImage;


    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    public ChatAdapter(List<ChatMessage> chatMessages,Bitmap receiverProfileImage, String senderId, Context context) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.context = context;
        this.receiverProfileImage = receiverProfileImage;

    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ContainerChatBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false
                    ));
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceivedMessageHolder(
                    ContainerChat1Binding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );
        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            return new SentImageMessageViewHolder(
                    ContainerchatimageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false
                    ));
        } else { // VIEW_TYPE_RECEIVED_IMAGE
            return new ReceivedImageMessageHolder(
                    Containerchatimage1Binding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setTextData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            ((ReceivedMessageHolder) holder).setTextData(chatMessages.get(position), receiverProfileImage);
        } else if (getItemViewType(position) == VIEW_TYPE_SENT_IMAGE) {
            ((SentImageMessageViewHolder) holder).setImageData(chatMessages.get(position));
        } else { // VIEW_TYPE_RECEIVED_IMAGE
            ((ReceivedImageMessageHolder) holder).setImageData(receiverProfileImage, chatMessages.get(position));
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showOptionsDialog(position);
                return true;
            }
        });
    }

    private void showOptionsDialog(int position)
    {
        MessageOptionsDialog dialog = new MessageOptionsDialog(new MessageOptionsDialog.MessageOptionListener() {
            @Override
            public void onCopyClicked() {
                ChatMessage selectedMessage = chatMessages.get(position); // Assuming you have a variable for the selected message
                String messageText = selectedMessage.message;

                // Copy the message text to the clipboard
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Message", messageText);
                clipboardManager.setPrimaryClip(clipData);

                // Notify the user that the message has been copied
                Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClicked() {
                ChatMessage selectedMessage = chatMessages.get(position); // Assuming you have a variable for the selected message
                int selectedPosition = position; // Store the selected position

                // Perform the delete operation in your database
                // For example, if you have a Firestore database, you can delete the message as follows:
                String messageId = selectedMessage.getMessageId(); // Replace getMessageId() with the actual method to get the message ID
                Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
                FirebaseFirestore.getInstance().collection(Constants.KEY_CHAT)
                        .document(messageId) // Use messageId instead of message
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Message deleted successfully
                                // Now, remove the deleted message from your chatMessages list
                                removeMessageAndUpdateUI(selectedPosition);
                                Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle the failure to delete the message
                                Toast.makeText(context, "Failed to delete message", Toast.LENGTH_SHORT).show();
                            }
                        });
            }


            private void removeMessageAndUpdateUI(int position) {
                // Create a copy of the chatMessages list and remove the deleted message from it
                List<ChatMessage> updatedChatMessages = new ArrayList<>(chatMessages);
                updatedChatMessages.remove(position);

                // Update the chatMessages list and notify the adapter
                chatMessages.clear();
                chatMessages.addAll(updatedChatMessages);
                notifyDataSetChanged();
            }



            public void onForwardClicked() {
                ChatMessage selectedMessage = chatMessages.get(position); // Assuming you have a variable for the selected message

                // Launch an activity or dialog to select a user to forward the message to
                Intent forwardIntent = new Intent(context, UserPage.class);
                forwardIntent.putExtra("selectedMessage", selectedMessage.message);
                context.startActivity(forwardIntent);
            }


            public void onReactClicked() {

                            }


        });

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "MessageOptionsDialog");
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId))
        {
            return VIEW_TYPE_SENT;
        }
        else
        {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ContainerChatBinding binding;

        SentMessageViewHolder(ContainerChatBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setTextData(ChatMessage chatMessage) {
            binding.textChat1.setVisibility(View.VISIBLE);
            binding.textChat1.setText(chatMessage.message);
            binding.textChat3.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private final ContainerChat1Binding binding;

        ReceivedMessageHolder(ContainerChat1Binding containerChat1Binding) {
            super(containerChat1Binding.getRoot());
            binding = containerChat1Binding;
        }

        void setTextData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textChat1.setVisibility(View.VISIBLE);
            binding.textChat1.setText(chatMessage.message);
            binding.textChat3.setText(chatMessage.dateTime);
            binding.imageuser.setImageBitmap(receiverProfileImage);
        }
    }


    static class SentImageMessageViewHolder extends RecyclerView.ViewHolder {
        private final ContainerchatimageBinding binding;

        SentImageMessageViewHolder(ContainerchatimageBinding itemContainerSentImageMessageBinding) {
            super(itemContainerSentImageMessageBinding.getRoot());
            binding = itemContainerSentImageMessageBinding;
        }

        void setImageData(ChatMessage chatMessage) {
            binding.senderImage.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext())
                    .load(chatMessage.getImageUrl()) // Assuming chatMessage.getImageUrl() returns the image URL
                    .into(binding.senderImage);
        }
    }
    static class ReceivedImageMessageHolder extends RecyclerView.ViewHolder {
        private final Containerchatimage1Binding binding;

        ReceivedImageMessageHolder(Containerchatimage1Binding containerChatImage1Binding) {
            super(containerChatImage1Binding.getRoot());
            binding = containerChatImage1Binding;
        }

        void setImageData(Bitmap receiverProfileImage, ChatMessage chatMessage) {
            binding.receiverImage.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext())
                    .load(chatMessage.getImageUrl()) // Assuming chatMessage.getImageUrl() returns the image URL
                    .into(binding.receiverImage);
            binding.imageuser.setImageBitmap(receiverProfileImage);
        }
    }



}

