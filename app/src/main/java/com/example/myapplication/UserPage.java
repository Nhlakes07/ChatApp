package com.example.myapplication;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.myapplication.adapters.ConversationAdapter;
import com.example.myapplication.adapters.TaskAdapter;
import com.example.myapplication.adapters.UsersAdapter;
import com.example.myapplication.databinding.ActivityUserPageBinding;
import com.example.myapplication.listeners.ConversationListener;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.TaskItem;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class UserPage extends AppCompatActivity  implements ConversationListener, UserListener {

    private PreferenceManager preferenceManager;
    TextView uName;
    ImageView image;

    private ActivityUserPageBinding binding;

    private List<ChatMessage> convesations;

    private ConversationAdapter conversationAdapter;

    private FirebaseFirestore firestore;

    private MeowBottomNavigation meowBottomNavigation;

    Button btnPickDateTime;
    EditText editTextDueDate,editTextTask,editTextDescription;

    TextView dates;

    ListView listViewTasks;

    CardView recentChat;

    ProgressBar progressBar;
    TextView text;
    private int count=0;
    private ListView userListView;
    private UsersAdapter usersListAdapter;

    private SearchView searchView;

    private boolean isSearchViewVisible = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        binding = ActivityUserPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();



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

        meowBottomNavigation= findViewById(R.id.mewoView);
        binding.message.setText("Chats");

        meowBottomNavigation.add(new MeowBottomNavigation.Model(2,R.drawable.baseline_mark_chat_unread_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(3,R.drawable.settings));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(6,R.drawable.finduser));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(5,R.drawable.baseline_playlist_list));

        recentChat = findViewById(R.id.widgetuser1);

        meowBottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {

                switch (model.getId())
                {
                    case 2:
                        binding.widgetuser1.setVisibility(View.VISIBLE);
                        binding.widgetuser2.setVisibility(View.GONE);
                        binding.widgetuser3.setVisibility(View.GONE);
                        binding.widgetuser6.setVisibility(View.GONE);
                        binding.message.setText("Chats");
                        break;
                    case 3:
                        binding.widgetuser2.setVisibility(View.VISIBLE);
                        binding.widgetuser1.setVisibility(View.GONE);
                        binding.widgetuser3.setVisibility(View.GONE);
                        binding.widgetuser6.setVisibility(View.GONE);
                        binding.message.setText("Settings");
                        break;
                    case 6:
                        binding.widgetuser6.setVisibility(View.VISIBLE);
                        binding.widgetuser1.setVisibility(View.GONE);
                        binding.widgetuser2.setVisibility(View.GONE);
                        binding.widgetuser3.setVisibility(View.GONE);
                        binding.message.setText("Users");
                        break;
                    case 5:
                        binding.widgetuser3.setVisibility(View.VISIBLE);
                        binding.widgetuser1.setVisibility(View.GONE);
                        binding.widgetuser2.setVisibility(View.GONE);
                        binding.widgetuser6.setVisibility(View.GONE);
                        binding.message.setText("Task");
                        break;
                }
                return null;
            }
        });

        preferenceManager = new PreferenceManager(getApplicationContext());
        uName = findViewById(R.id.UName);

        RecyclerView recyclerView = findViewById(R.id.recycleusers);
        final FrameLayout frameLayout = findViewById(R.id.frameLayout);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    // Scrolling downwards
                    frameLayout.setVisibility(View.VISIBLE);
                } else if (dy < 0) {
                    // Scrolling upwards
                    frameLayout.setVisibility(View.GONE);
                }
            }
        });
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversation();




        userListView = findViewById(R.id.userListView);
        progressBar = findViewById(R.id.progressBar); // Assuming you have a ProgressBar with this ID
        text = findViewById(R.id.errorText); // Assuming you have a TextView with this ID

        searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);

        ImageView findUserImageView = findViewById(R.id.ImageView);
        findUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchViewVisibility();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the list when the query text changes
                usersListAdapter.filter(newText);
                return true;
            }
        });

        preferenceManager = new PreferenceManager(getApplicationContext());
        usersListAdapter = new UsersAdapter(this, new ArrayList<>(),this);
        userListView.setAdapter(usersListAdapter);
        getUser();
    }

    public void init()
    {
        convesations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(convesations, this);
        binding.recycleusers.setAdapter(conversationAdapter);
        firestore = FirebaseFirestore.getInstance();
    }

    private void loadUserDetails()
    {

         uName.setText(preferenceManager.getString(Constants.KEY_NAME));

    }

    public void setToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversation()
    {
        firestore.collection(Constants.KEY_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        firestore.collection(Constants.KEY_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value,error) ->
    {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    String datetime = getReadableDate(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    chatMessage.dateTime = datetime;
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId))
                    {
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                    }
                    else
                    {
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);

                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    convesations.add(chatMessage);
                }
                else if (documentChange.getType()==DocumentChange.Type.MODIFIED)
                {
                     for (int i=0;i<convesations.size();i++)
                     {
                         String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                         String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                         if (convesations.get(i).senderId.equals(senderId) && convesations.get(i).receiverId.equals(receiverId))
                         {
                            convesations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            convesations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                         }

                     }
                }

            }
            Collections.sort(convesations,(obj1,obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.recycleusers.smoothScrollToPosition(0);
            binding.recycleusers.setVisibility(View.VISIBLE);
            binding.progressUsers.setVisibility(View.GONE);
        }
    };


    public void updateToken(String token)
    {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_TOKEN,token).addOnSuccessListener(unused -> setToast(""))
                .addOnFailureListener(e -> setToast("Unable to update token"));
    }

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    public void SignOut()
    {
        setToast("Logging out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(),Login.class));
            finish();
        }).addOnFailureListener(e -> setToast("Unable to Logout"));
    }



    public void setListeners() {


        binding.AccountManagement.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),AccountManagement.class)));

    }

    @Override
    public void onConversionListener(Users users) {
        Intent intent = new Intent(getApplicationContext(),ChatScreen.class);
        intent.putExtra(Constants.KEY_USER,users);
        startActivity(intent);

        Intent intent1 = new Intent(getApplicationContext(), To_do.class);
        // Pass the user ID as an extra
        intent.putExtra(To_do.EXTRA_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        startActivity(intent);

    }
    private String getReadableDate (Date date){

        long currentTimeMillis = System.currentTimeMillis();
        long timeDifferenceMillis = currentTimeMillis - date.getTime();

        // Check if the time difference is greater than or equal to 24 hours (in milliseconds)
        if (timeDifferenceMillis >= 7 * 24 * 60 * 60 * 1000) {
            // Show the date in Year/Month/Day format
            return new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(date);
        } else if (timeDifferenceMillis >= 24 * 60 * 60 * 1000) {
            // Include the day of the week
            return new SimpleDateFormat("E hh:mm a", Locale.getDefault()).format(date);
        } else {
            // Exclude the day of the week
            return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
        }


    }

    public void Profile(View view)
    {
        Users users = new Users();

        users.id = preferenceManager.getString(Constants.KEY_USER_ID);
        users.Name = preferenceManager.getString(Constants.KEY_NAME);
        users.Surname = preferenceManager.getString(Constants.KEY_SURNAME);
        users.Email = preferenceManager.getString(Constants.KEY_EMAIL);
        users.PhoneNumber = preferenceManager.getString(Constants.KEY_PHONENUMBER);
        users.image = preferenceManager.getString(Constants.KEY_IMAGE);// or wherever you get the user ID
        Intent intent = new Intent(getApplicationContext(), ProfileUser.class);
        intent.putExtra(Constants.KEY_USER, users);
        startActivity(intent);
    }

    public void LogOut(View view)
    {
        SignOut();
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
                                UserPage.this,
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
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
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

    private void loadTasks() {
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

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

    private void toggleSearchViewVisibility()
    {
        isSearchViewVisible = !isSearchViewVisible;
        if (isSearchViewVisible) {
            searchView.setVisibility(View.VISIBLE);
            searchView.setIconified(false); // Expand the SearchView
        } else {
            searchView.setVisibility(View.GONE);
            searchView.setQuery("", false); // Clear the search query
        }
    }
    public void getUser ()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task ->
        {
            loading(false);
            String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
            if (task.isSuccessful() && task.getResult() != null)
            {
                List<Users> users = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                {
                    if(currentUserId.equals(queryDocumentSnapshot.getId())) {
                        continue;
                    }
                    Users user = new Users();
                    user.Name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                    user.Surname = queryDocumentSnapshot.getString(Constants.KEY_SURNAME);
                    user.Email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                    user.PhoneNumber = queryDocumentSnapshot.getString(Constants.KEY_PHONENUMBER);
                    user.Token = queryDocumentSnapshot.getString(Constants.KEY_TOKEN);
                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                    user.id = queryDocumentSnapshot.getId();

                    users.add(user);

                }

                if(!users.isEmpty())
                {
                    usersListAdapter.updateData(users);
                    userListView.setVisibility(View.VISIBLE);

                } else {
                    // Handle any errors that occurred during the data retrieval

                    showErrorMessage();

                }
            }else{
                showErrorMessage();
            }
        });
    }
    public void showErrorMessage() {
        // Handle and display an error message
        text.setText(String.format("%s", "No users found"));
        text.setVisibility(View.VISIBLE);
    }

    public void loading(Boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(Users users) {

    }
    public void backtouserpage(View view)
    {
        Intent intent = new Intent(this,UserPage.class);
        Bundle b = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        startActivity(intent , b);

    }

}