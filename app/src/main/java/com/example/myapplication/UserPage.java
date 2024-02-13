package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.myapplication.adapters.ConversationAdapter;
import com.example.myapplication.databinding.ActivityUserPageBinding;
import com.example.myapplication.listeners.ConversationListener;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class UserPage extends AppCompatActivity  implements ConversationListener {

    private PreferenceManager preferenceManager;
    TextView uName;
    ImageView image;

    private ActivityUserPageBinding binding;

    private List<ChatMessage> convesations;

    private ConversationAdapter conversationAdapter;

    private FirebaseFirestore firestore;

    private MeowBottomNavigation meowBottomNavigation;

    CardView recentChat;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        binding = ActivityUserPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        meowBottomNavigation= findViewById(R.id.mewoView);
        binding.message.setText("Chats");

        meowBottomNavigation.add(new MeowBottomNavigation.Model(2,R.drawable.baseline_mark_chat_unread_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(3,R.drawable.settings));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(4,R.drawable.users));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(5,R.drawable.calls_24));

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
                        binding.message.setText("Chats");
                        break;
                    case 3:
                        binding.widgetuser2.setVisibility(View.VISIBLE);
                        binding.widgetuser1.setVisibility(View.GONE);
                        binding.widgetuser3.setVisibility(View.GONE);
                        binding.message.setText("Settings");
                        break;
                    case 4:
                        startActivity(new Intent(getApplicationContext(),UsersActivity.class));
                        break;
                    case 5:
                        binding.widgetuser3.setVisibility(View.VISIBLE);
                        binding.widgetuser1.setVisibility(View.GONE);
                        binding.widgetuser2.setVisibility(View.GONE);
                        binding.message.setText("Calls");
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
        binding.ToDo.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),To_do.class)));

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
}