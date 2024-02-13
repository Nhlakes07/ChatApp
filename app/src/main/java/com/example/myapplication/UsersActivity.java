package com.example.myapplication;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.UsersAdapter;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {

    ProgressBar progressBar;
    TextView text;
    private int count=0;
    private ListView userListView;
    private PreferenceManager preferenceManager;
    private UsersAdapter usersListAdapter;

    private SearchView searchView;

    private boolean isSearchViewVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_users);

        // Initialize views
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
