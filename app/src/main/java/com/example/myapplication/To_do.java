package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.TaskAdapter;
import com.example.myapplication.models.TaskItem;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class To_do extends AppCompatActivity {

    Button btnPickDateTime;
    EditText editTextDueDate,editTextTask,editTextDescription;

    TextView dates;
    private PreferenceManager preferenceManager;

    ListView listViewTasks;
    public static final String EXTRA_USER_ID = "user_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_to_do);


        listViewTasks = findViewById(R.id.listViewTasks);
        btnPickDateTime = findViewById(R.id.editTextDueDate);
        editTextTask = findViewById(R.id.editTextTask);
        editTextDescription = findViewById(R.id.editTextDescription);
        dates = findViewById(R.id.date);

        loadTasks();

        btnPickDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerDialog();
            }
        });
    }
        private void showDateTimePickerDialog() {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Create a date picker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            // Set chosen date to the calendar
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            // Create a time picker dialog
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    To_do.this,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                            // Set chosen time to the calendar
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                            calendar.set(Calendar.MINUTE, minute);

                                            // Format the chosen date and time
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                            String formattedDateTime = dateFormat.format(calendar.getTime());

                                            // Set the formatted date and time to the EditText
                                            dates.setText(formattedDateTime);
                                        }
                                    },
                                    hour,
                                    minute,
                                    false
                            );

                            // Show the time picker dialog
                            timePickerDialog.show();
                        }
                    },
                    year,
                    month,
                    day
            );

            // Show the date picker dialog
            datePickerDialog.show();
        }

        public void showmessge(String message)
        {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }


    public void Save(View view) {
        Users users = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        String task = editTextTask.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = dates.getText().toString().trim();

        // Check if all fields are filled
        if (task.isEmpty() || description.isEmpty() || date.isEmpty()) {
            // Display an error message if any field is empty
            showmessge("Please fill in all fields");
            return;
        }

        // Check if the task already exists in Firestore
        database.collection(Constants.KEY_TASKS)
                .whereEqualTo(Constants.KEY_TASK_ID, userId)
                .whereEqualTo(Constants.KEY_TASK, task)
                .whereEqualTo(Constants.KEY_DESC, description)
                .whereEqualTo(Constants.KEY_DATE, date)
                .get()
                .addOnSuccessListener(taskQuerySnapshot -> {
                    if (taskQuerySnapshot.isEmpty()) {
                        // Task doesn't exist, proceed with saving
                        HashMap<String, Object> taskData = new HashMap<>();
                        taskData.put(Constants.KEY_TASK_ID,userId);
                        taskData.put(Constants.KEY_TASK, task);
                        taskData.put(Constants.KEY_DESC, description);
                        taskData.put(Constants.KEY_DATE, date);

                        database.collection(Constants.KEY_TASKS)
                                .add(taskData)
                                .addOnSuccessListener(documentReference -> {
                                    showmessge("Task saved successfully");

                                    // Reload tasks immediately after adding
                                    loadTasks();
                                })
                                .addOnFailureListener(exception -> {
                                    // Task failed to save
                                    showmessge("Task not saved");
                                });
                    } else {
                        // Task already exists
                        showmessge("Task already exists");
                    }
                })
                .addOnFailureListener(exception -> {
                    // Error checking for existing task
                    showmessge("Error checking for existing task");
                });
    }

    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());


    }


    private void loadTasks() {
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = null;

        database.collection(Constants.KEY_TASKS)
                .whereEqualTo(Constants.KEY_TASK_ID, userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<TaskItem> taskList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String task = document.getString(Constants.KEY_TASK);
                        String description = document.getString(Constants.KEY_DESC);
                        String date = document.getString(Constants.KEY_DATE);


                        TaskItem taskItem = new TaskItem(task, description, date);

                        String task1 = taskItem.getTask();
                        String description1 = taskItem.getDescription();
                        String date1 = taskItem.getDate();

                        checkDueDateWithinTwoDays(task1,description1,date1);
                        checkDueDateWithinWeek(task1,description1,date1);
                        taskList.add(taskItem);
                    }

                    // Create a custom adapter
                    TaskAdapter taskAdapter = new TaskAdapter(this, taskList);

                    // Set the custom adapter to your ListView
                    listViewTasks.setAdapter(taskAdapter);
                })
                .addOnFailureListener(exception -> {
                    // Handle failure to fetch tasks
                });
    }

    private void checkDueDateWithinWeek(String dueDate,String task,String description) {
        // Parse the due date string to a Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date dueDateObj;
        try {
            dueDateObj = dateFormat.parse(dueDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return; // Unable to parse due date, exit the method
        }

        // Calculate the difference between current time and due date
        long timeDifference = dueDateObj.getTime() - System.currentTimeMillis();
        long daysDifference = TimeUnit.MILLISECONDS.toDays(timeDifference);

        // Show a pop-up message if due date is within a week
        if (daysDifference <= 7 && daysDifference >= 0) {
            showPopupMessage("Task is due within a week", task, description, dueDate);
        }
    }

    private void checkDueDateWithinTwoDays(String dueDate,String task,String description) {
        // Parse the due date string to a Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date dueDateObj;
        try {
            dueDateObj = dateFormat.parse(dueDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return; // Unable to parse due date, exit the method
        }

        // Calculate the difference between current time and due date
        long timeDifference = dueDateObj.getTime() - System.currentTimeMillis();
        long daysDifference = TimeUnit.MILLISECONDS.toDays(timeDifference);

        // Show a pop-up message if due date is within 2 days
        if (daysDifference <= 2 && daysDifference >= 0) {
            showPopupMessage("Task is due within 2 days", task, description, dueDate);
        }
    }



    private void showPopupMessage(String message, String task, String description, String dueDate) {
        // Use AlertDialog or any other pop-up method to display the message
        // Example using AlertDialog:
        new AlertDialog.Builder(this)
                .setTitle("Due Date Alert")
                .setMessage(message + "\n\nTask: " + task + "\nDescription: " + description + "\nDue Date: " + dueDate)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with your action or close the dialog
                    }
                })
                .show();
    }

}

