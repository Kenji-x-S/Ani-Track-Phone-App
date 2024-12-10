package com.example.ani_track;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvProfileEmail, tvWatchlistTitle, tvUsername;
    private RecyclerView rvWatchlist;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private UserProfileAdapter aAdapter;
    private List<Anime> animeList;
    private String currentUsername, profileUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getWindow().setStatusBarColor(Color.parseColor("#121212"));
        // Initialize views
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvWatchlistTitle = findViewById(R.id.tvWatchlistTitle);
        rvWatchlist = findViewById(R.id.rvWatchlist);
        tvUsername=findViewById(R.id.tvProfileName);
        // Set up RecyclerView
        rvWatchlist.setLayoutManager(new LinearLayoutManager(this));
        animeList = new ArrayList<>();
        aAdapter = new UserProfileAdapter(animeList);
        rvWatchlist.setAdapter(aAdapter);

        // Get passed data from intent
        currentUsername = getIntent().getStringExtra("currentUsername");
        profileUsername = getIntent().getStringExtra("username");
        tvUsername.setText(profileUsername);
        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("people").child(profileUsername);

        // Load user data from Firebase
        loadUserProfile();
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get user data
                if (dataSnapshot.exists()) {
                    // Get the email of the user
                    String email = dataSnapshot.child("email").getValue(String.class);
                    tvProfileEmail.setText(email);

                    // Load the watchlist (anime list)
                    loadWatchlist(dataSnapshot.child("watchlist"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(UserProfileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWatchlist(DataSnapshot watchlistSnapshot) {
        for (DataSnapshot animeSnapshot : watchlistSnapshot.getChildren()) {
            // Retrieve anime data for each anime in the watchlist
            int animeId = animeSnapshot.child("animeId").getValue(Integer.class);
            String imageUrl = animeSnapshot.child("imageUrl").getValue(String.class);
            String title = animeSnapshot.child("title").getValue(String.class);
            String description = animeSnapshot.child("description").getValue(String.class);
            String status = animeSnapshot.child("status").getValue(String.class);

            // Create Anime object and add it to the list
            Anime anime = new Anime(title, description, imageUrl, animeId, status);
            animeList.add(anime);
        }

        // Notify adapter that data has been added
        aAdapter.notifyDataSetChanged();
    }

    // Adapter for RecyclerView
    public static class UserProfileAdapter extends RecyclerView.Adapter<UserProfileAdapter.AViewHolder> {

        private List<Anime> animeList;

        public UserProfileAdapter(List<Anime> animeList) {
            this.animeList = animeList;
        }

        @NonNull
        @Override
        public AViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.anime_layout_profile, parent, false);
            return new AViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AViewHolder holder, int position) {
            Anime anime = animeList.get(position);

            // Set data to views
            Picasso.get().load(anime.getImageUrl()).into(holder.animeImage);
            holder.animeTitle.setText(anime.getTitle());
            holder.animeStatus.setText("Status: " + anime.getStatus());
        }

        @Override
        public int getItemCount() {
            return animeList.size();
        }

        public static class AViewHolder extends RecyclerView.ViewHolder {
            ImageView animeImage;
            TextView animeTitle, animeStatus;

            public AViewHolder(View itemView) {
                super(itemView);
                animeImage = itemView.findViewById(R.id.animeImage);
                animeTitle = itemView.findViewById(R.id.animeTitle);
                animeStatus = itemView.findViewById(R.id.animeStatus);
            }
        }
    }
}
