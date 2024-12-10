package com.example.ani_track;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;

public class AnimeActivity extends AppCompatActivity {

    private EditText searchAnime;
    private RecyclerView recyclerView;
    private AnimeAdapter animeAdapter;
    private List<Anime> animeList;
    private TextView tabPeople, tabWatchlist,tabAnime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animetab);
        getWindow().setStatusBarColor(Color.parseColor("#333333"));
        // Initializing UI components
        searchAnime = findViewById(R.id.searchBar);
        recyclerView = findViewById(R.id.recyclerViewAnime);
        tabPeople = findViewById(R.id.tabPeople);
        tabWatchlist = findViewById(R.id.tabWatchlist);
        tabAnime=findViewById(R.id.tabAnime);
        tabAnime.setTextColor(Color.WHITE);
        tabAnime.setBackgroundResource(R.drawable.tab_highlight);
        ImageView logoutIcon = findViewById(R.id.logoutIcon);

        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog to confirm logout
                AlertDialog.Builder builder = new AlertDialog.Builder(AnimeActivity.this);
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


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize list and adapter
        animeList = new ArrayList<>();
        animeAdapter = new AnimeAdapter(animeList, this);
        recyclerView.setAdapter(animeAdapter);

        // Firebase reference (if needed for future use)
        String username = getIntent().getStringExtra("username");

        // Fetch all anime data initially
        fetchAnimeData("");

        // Real-time search listener
        searchAnime.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!TextUtils.isEmpty(charSequence)) {
                    // Perform live search when user types in the search bar
                    fetchAnimeData(charSequence.toString());
                } else {
                    // If search bar is cleared, display all anime again
                    fetchAnimeData("");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        tabPeople.setOnClickListener(view -> {
            Intent intent = new Intent(AnimeActivity.this, PeopleActivity.class);
            // Pass the username via Intent
            intent.putExtra("username", username);
            startActivity(intent);  // Start PeopleActivity
        });

// Set click listener for tabWatchlist TextView
        tabWatchlist.setOnClickListener(view -> {
            Intent intent = new Intent(AnimeActivity.this, WatchlistActivity.class);
            // Pass the username via Intent
            intent.putExtra("username", username);
            startActivity(intent);  // Start WatchlistActivity
        });
    }

    public void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("isLoggedIn");
        editor.apply();

        Intent signInIntent = new Intent(AnimeActivity.this, MainActivity.class);
        signInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(signInIntent);
        finish();
    }
    private void fetchAnimeData(String query) {
        // Set URL based on the search query
        String url;
        if (TextUtils.isEmpty(query)) {
            url = "https://api.jikan.moe/v4/anime"; // Default API to get all anime
        } else {
            url = "https://api.jikan.moe/v4/anime?q=" + query; // Search API for specific query
        }

        // Creating OkHttpClient to make the API call
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        // Making the API call asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AnimeActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Parse the response data
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        JSONArray results;
                        results = jsonObject.getJSONArray("data");

                        animeList.clear();
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject anime = results.getJSONObject(i);
                            String title = anime.getString("title");
                            String description = anime.optString("synopsis", "No description available");
                            JSONObject images = anime.getJSONObject("images");
                            JSONObject jpgImage = images.getJSONObject("jpg");
                            String imageUrl = jpgImage.getString("image_url");
                            int animeId = anime.getInt("mal_id");
                            String status="Watching";
                            // Create Anime object and add it to the list
                            Anime animeItem = new Anime(title, description, imageUrl, animeId, status);
                            animeList.add(animeItem);
                        }

                        // Notify the adapter on the main thread to update UI
                        runOnUiThread(() -> animeAdapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
