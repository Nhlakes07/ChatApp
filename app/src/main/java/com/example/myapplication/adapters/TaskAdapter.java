package com.example.myapplication.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.myapplication.models.TaskItem;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<TaskItem>
{
        private Context context;
        private List<TaskItem> taskList;

        public TaskAdapter(@NonNull Context context, @NonNull List<TaskItem> taskList) {
            super(context, 0, taskList);
            this.context = context;
            this.taskList = taskList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(context).inflate(R.layout.task_list_item, parent, false);
            }

            // Get the current task item
            TaskItem currentTask = taskList.get(position);

            // Set the task, description, and date to the TextViews in your custom layout
            TextView taskTextView = listItemView.findViewById(R.id.taskTextView);
            taskTextView.setText(currentTask.getTask());

            TextView descriptionTextView = listItemView.findViewById(R.id.descriptionTextView);
            descriptionTextView.setText(currentTask.getDescription());

            TextView dateTextView = listItemView.findViewById(R.id.dateTextView);
            dateTextView.setText(currentTask.getDate());

            return listItemView;
        }
    }


