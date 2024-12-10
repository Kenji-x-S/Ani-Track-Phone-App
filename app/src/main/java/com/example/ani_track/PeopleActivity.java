package com.example.ani_track;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.SharedPreferences;

public class PeopleActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPeople;
    private EditText searchBarPeople;
    private TextView tabAnime, tabPeople, tabWatchlist;
    private String currentUsername; // Username passed from previous activity
    private List<User> userList = new ArrayList<>();
    private PeopleAdapter peopleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        getWindow().setStatusBarColor(Color.parseColor("#121212"));

        recyclerViewPeople = findViewById(R.id.recyclerViewPeople);
        searchBarPeople = findViewById(R.id.searchBarPeople);
        tabAnime = findViewById(R.id.tabAnime);
        tabPeople = findViewById(R.id.tabPeople);
        tabWatchlist = findViewById(R.id.tabWatchlist);
        tabPeople.setTextColor(Color.WHITE);
        tabPeople.setBackgroundResource(R.drawable.tab_highlight);
        ImageView logoutIcon = findViewById(R.id.logoutIcon);

        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog to confirm logout
                AlertDialog.Builder builder = new AlertDialog.Builder(PeopleActivity.this);
                builder.setMessage("Are you sure you want to logout?")
                        .setCancelable(false) // Prevent dismiss by tapping outside
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                logout();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Dismiss the dialog and return to the current activity without doing anything
                                dialog.dismiss();
                            }
                        });

                // Create and show the dialog
                AlertDialog alert = builder.create();
                alert.show();

                // Change the color of the buttons (Red for logout, White for cancel)
                Button logoutButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                logoutButton.setTextColor(Color.RED);

                Button cancelButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                cancelButton.setTextColor(Color.WHITE);
            }
        });


        // Get the username from the intent
        currentUsername = getIntent().getStringExtra("username");

        // Initialize RecyclerView
        recyclerViewPeople.setLayoutManager(new LinearLayoutManager(this));
        peopleAdapter = new PeopleAdapter(userList, this::onUserClicked);
        recyclerViewPeople.setAdapter(peopleAdapter);

        // Load users from database
        loadUsersFromDatabase();

        // Set up search functionality
        searchBarPeople.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up tab clicks
        tabAnime.setOnClickListener(v -> {
            Intent intent = new Intent(PeopleActivity.this, AnimeActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        tabWatchlist.setOnClickListener(v -> {
            Intent intent = new Intent(PeopleActivity.this, WatchlistActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
    }

    private void loadUsersFromDatabase() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("people");
        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                userList.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    // Check if the value is a map or an array and handle accordingly
                    if (snapshot.getValue() instanceof Map) {
                        Map<String, Object> userMap = (Map<String, Object>) snapshot.getValue();
                        User user = mapToUser(snapshot.getKey(), userMap);
                        if (user != null && user.getUsername() != null && !user.getUsername().equals(currentUsername)) {
                            userList.add(user);
                        }
                    } else if (snapshot.getValue() instanceof ArrayList) {
                        // Handle ArrayList if that's the expected format
                        ArrayList<Object> userListArray = (ArrayList<Object>) snapshot.getValue();
                        // Process the ArrayList, for example, you could cast objects inside this list to Map if needed
                    }
                }
                peopleAdapter.notifyDataSetChanged();
            }
        });
    }


    public void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("isLoggedIn");
        editor.apply();

        Intent signInIntent = new Intent(PeopleActivity.this, MainActivity.class);
        signInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(signInIntent);
        finish();
    }

    private void filterUsers(String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        peopleAdapter.updateList(filteredList);
    }

    private void onUserClicked(User user) {
        Intent intent = new Intent(PeopleActivity.this, UserProfileActivity.class);
        intent.putExtra("currentUsername", currentUsername);  // Pass your own username
        intent.putExtra("username", user.getUsername());       // Pass the clicked person's username
        startActivity(intent);
    }

    private User mapToUser(String username, Object userData) {
        Log.d("mapToUser", "userData type: " + userData.getClass().getName());
        if (userData == null) return null;

        String profileImageUrl = "https://cdn.discordapp.com/attachments/852883226817069086/1316108276353732721/ani-track.png?ex=6759d8d9&is=67588759&hm=6a86f1dd852bbf0794df6bcd3db50d5beb88fa6f556ec0919215be47af7be91f&";  // Default image URL
        String email = null;

        // Check if the data is a Map (single user)
        if (userData instanceof Map) {
            Map<String, Object> userMap = (Map<String, Object>) userData;

            // Retrieve email
            email = (String) userMap.get("email");

            // Retrieve watchlist if available
            Object watchlistObj = userMap.get("watchlist");
            if (watchlistObj != null && watchlistObj instanceof Map) {
                Map<String, Object> watchlist = (Map<String, Object>) watchlistObj;
                if (watchlist != null && !watchlist.isEmpty()) {
                    // Get the first anime from the watchlist (map entry)
                    Map<String, Object> firstAnime = (Map<String, Object>) watchlist.values().iterator().next();
                    if (firstAnime != null && firstAnime.containsKey("imageUrl")) {
                        profileImageUrl = (String) firstAnime.get("imageUrl");
                    }
                }
            } else {
                // Log if the watchlist is not available
                Log.w("mapToUser", "Watchlist is not a Map or is missing.");
            }
        }
        // If the data is a List (multiple users), handle it accordingly
        else if (userData instanceof ArrayList) {
            ArrayList<Map<String, Object>> userList = (ArrayList<Map<String, Object>>) userData;

            // Process the first user from the list
            if (!userList.isEmpty()) {
                Map<String, Object> userMap = userList.get(0);  // Assuming you want the first user
                email = (String) userMap.get("email");

                // Retrieve watchlist if available
                Object watchlistObj = userMap.get("watchlist");
                if (watchlistObj != null && watchlistObj instanceof Map) {
                    Map<String, Object> watchlist = (Map<String, Object>) watchlistObj;
                    if (watchlist != null && !watchlist.isEmpty()) {
                        Map<String, Object> firstAnime = (Map<String, Object>) watchlist.values().iterator().next();
                        if (firstAnime != null && firstAnime.containsKey("imageUrl")) {
                            profileImageUrl = (String) firstAnime.get("imageUrl");
                        }
                    }
                }
            }
        }

        // Create the User object
        User user = new User();
        user.setUsername(username);  // Username is the key
        user.setEmail(email);        // Set the email
        user.setProfileImageUrl(profileImageUrl);  // Set the profile image URL

        return user;
    }


}