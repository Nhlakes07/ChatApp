package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.ChatScreen;
import com.example.myapplication.R;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends BaseAdapter {

    private final List<Users> usersList;
    private final Context context;
    private List<Users> filteredList;
    private final UserListener userListener;

    public UsersAdapter(Context context, ArrayList<Users> usersList, UserListener userListener) {
        this.context = context;
        this.usersList = usersList;
        this.filteredList = new ArrayList<>(usersList);
        this.userListener = userListener;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void filter(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(usersList); // If query is empty, show all users
        } else {
            // Iterate through usersList and add matching users to the filtered list
            for (Users user : usersList) {
                if (user.Name.contains(query)) {
                    filteredList.add(user);
                }
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_container_view, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Users user = filteredList.get(position); // Use the filteredList for display

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatScreen.class);
            intent.putExtra(Constants.KEY_USER, user);
            context.startActivity(intent);
        });

        holder.nameTextView = convertView.findViewById(R.id.textName);
        holder.emailTextView = convertView.findViewById(R.id.textEmail);
        holder.ProfileImage = convertView.findViewById(R.id.imageView4);

        holder.nameTextView.setText(user.Name);
        holder.emailTextView.setText(user.Email);

        if (user.image != null && !user.image.isEmpty()) {
            holder.ProfileImage.setImageBitmap(getUserImage(user.image));
        } else {
            holder.ProfileImage.setImageResource(R.drawable.user); // Set default image resource
        }

        return convertView;
    }

    public void updateData(List<Users> newUsers) {
        usersList.addAll(newUsers);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        RoundedImageView ProfileImage;
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
