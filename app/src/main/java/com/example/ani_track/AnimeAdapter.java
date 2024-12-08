package com.example.ani_track;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder> {

    private List<Anime> animeList;
    private Context context;
    private DatabaseReference databaseReference;

    public AnimeAdapter(List<Anime> animeList, Context context) {
        this.animeList = animeList;
        this.context = context;
        String username = ((AnimeActivity) context).getIntent().getStringExtra("username");
        if (username != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("people").child(username).child("watchlist").child("watching");
        }
    }

    @NonNull
    @Override
    public AnimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.anime_item_layout, parent, false);
        return new AnimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimeViewHolder holder, int position) {
        Anime anime = animeList.get(position);
        holder.titleTextView.setText(anime.getTitle());
        holder.descriptionTextView.setText(anime.getDescription());
        TextView alreadyInWatchlistText = holder.itemView.findViewById(R.id.watchlistStatusText);

        // Glide for image loading
        Glide.with(context)
                .load(anime.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                .into(holder.animeImageView);

// Check if the anime is already in the watchlist and update the button's state
        databaseReference.child(String.valueOf(anime.getAnimeId())).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // If anime is in the watchlist, hide the button and show the text
                holder.addToWatchlistButton.setVisibility(View.GONE);
                holder.alreadyInWatchlistText.setVisibility(View.VISIBLE); // Show "Already in Watchlist" text
            } else {
                // If anime is not in the watchlist, show the button to add it
                holder.addToWatchlistButton.setVisibility(View.VISIBLE);
                holder.alreadyInWatchlistText.setVisibility(View.GONE); // Hide the text
            }
        });
        holder.addToWatchlistButton.setOnClickListener(v -> {
                addToWatchlist(anime); // Add anime to watchlist
                holder.addToWatchlistButton.setVisibility(View.GONE); // Hide the button after adding
                holder.alreadyInWatchlistText.setVisibility(View.VISIBLE); // Show the "Already in Watchlist" text
        });
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    public static class AnimeViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView descriptionTextView;
        ImageView animeImageView;
        Button addToWatchlistButton;
        TextView alreadyInWatchlistText;

        public AnimeViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.animeTitle);
            descriptionTextView = itemView.findViewById(R.id.animeDescription);
            animeImageView = itemView.findViewById(R.id.animeImage);
            addToWatchlistButton = itemView.findViewById(R.id.addToWatchlistButton);
            alreadyInWatchlistText = itemView.findViewById(R.id.watchlistStatusText);
        }
    }

    private void addToWatchlist(Anime anime) {
        // Add anime to Firebase under the "watching" node
        databaseReference.child(String.valueOf(anime.getAnimeId())).setValue(anime);
        Toast.makeText(context, anime.getTitle() + " added to watchlist", Toast.LENGTH_SHORT).show();
    }
}
