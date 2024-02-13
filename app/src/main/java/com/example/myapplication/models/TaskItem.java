package com.example.myapplication.models;

public class TaskItem
{
        private String task;
        private String description;
        private String date;

        public TaskItem(String task, String description, String date) {
            this.task = task;
            this.description = description;
            this.date = date;
        }

        public String getTask() {
            return task;
        }

        public String getDescription() {
            return description;
        }

        public String getDate() {
            return date;
        }
    }

