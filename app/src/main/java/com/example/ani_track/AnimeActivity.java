package com.example.ani_track;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
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

public class AnimeActivity extends AppCompatActivity {

    private EditText searchAnime;
    private RecyclerView recyclerView;
    private AnimeAdapter animeAdapter;
    private List<Anime> animeList;
    private DatabaseReference databaseReference;
    private TextView tabPeople, tabWatchlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animetab);

        // Initializing UI components
        searchAnime = findViewById(R.id.searchBar);
        recyclerView = findViewById(R.id.recyclerViewAnime);
        tabPeople = findViewById(R.id.tabPeople);
        tabWatchlist = findViewById(R.id.tabWatchlist);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize list and adapter
        animeList = new ArrayList<>();
        animeAdapter = new AnimeAdapter(animeList, this);
        recyclerView.setAdapter(animeAdapter);

        // Firebase reference (if needed for future use)
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("people").child(username).child("watchlist").child("watching");
        }

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
            startActivity(intent);  // Start PeopleActivity
        });

        // Set click listener for tabWatchlist TextView
        tabWatchlist.setOnClickListener(view -> {
            Intent intent = new Intent(AnimeActivity.this, WatchlistActivity.class);
            startActivity(intent);  // Start WatchlistActivity
        });
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

                            // Create Anime object and add it to the list
                            Anime animeItem = new Anime(title, description, imageUrl, animeId);
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
