package com.example.ani_track;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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
        getWindow().setStatusBarColor(Color.parseColor("#333333"));

        recyclerViewPeople = findViewById(R.id.recyclerViewPeople);
        searchBarPeople = findViewById(R.id.searchBarPeople);
        tabAnime = findViewById(R.id.tabAnime);
        tabPeople = findViewById(R.id.tabPeople);
        tabWatchlist = findViewById(R.id.tabWatchlist);

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
                    Map<String, Object> userMap = (Map<String, Object>) snapshot.getValue();
                    if (userMap != null) {
                        User user = mapToUser(snapshot.getKey(), userMap);
                        if (user != null && user.getUsername() != null && !user.getUsername().equals(currentUsername)) {
                            userList.add(user);
                        }
                    }
                }
                peopleAdapter.notifyDataSetChanged();
            }
        });
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

    private User mapToUser(String username, Map<String, Object> userMap) {
        if (userMap == null) return null;

        // Retrieve email
        String email = (String) userMap.get("email");

        // Default profile image URL
        String profileImageUrl = "https://cdn.myanimelist.net/images/anime/1465/142014.jpg";

        // Get the first anime in the watchlist, if it exists
        Map<String, Object> watchlist = (Map<String, Object>) userMap.get("watchlist");
        if (watchlist != null && !watchlist.isEmpty()) {
            // Get the first anime from the watchlist (map entry)
            Map<String, Object> firstAnime = (Map<String, Object>) watchlist.values().iterator().next();
            if (firstAnime != null && firstAnime.containsKey("imageUrl")) {
                profileImageUrl = (String) firstAnime.get("imageUrl");
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
