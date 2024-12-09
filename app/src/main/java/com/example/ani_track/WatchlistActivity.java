package com.example.ani_track;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;

public class WatchlistActivity extends AppCompatActivity {
    private RecyclerView recyclerViewWatchlist;
    private AnimeAdapterwl adapter;
    private List<Anime> originalList = new ArrayList<>();
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);
        getWindow().setStatusBarColor(Color.parseColor("#333333"));

        Log.d("WatchlistActivity", "onCreate started");

        // Initialize Views
        recyclerViewWatchlist = findViewById(R.id.recyclerViewWatchlist);
        recyclerViewWatchlist.setLayoutManager(new LinearLayoutManager(this));

        EditText searchBar = findViewById(R.id.searchBar);
        TextView tabAnime = findViewById(R.id.tabAnime);
        TextView tabPeople = findViewById(R.id.tabPeople);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not needed for this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filter the list when text is changed
                filterAnime(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed for this case
            }
        });
        // Add Logging for View Initialization
        Log.d("WatchlistActivity", "Views initialized");

        // Add Navigation Handlers
        tabAnime.setOnClickListener(v -> {
            Intent intent = new Intent(WatchlistActivity.this, AnimeActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        tabPeople.setOnClickListener(v -> {
            Intent intent = new Intent(WatchlistActivity.this, PeopleActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Retrieve Intent Data
        username = getIntent().getStringExtra("username");
        Log.d("WatchlistActivity", "Username: " + username);

        if (username == null) {
            Log.e("WatchlistActivity", "Username is null, exiting");
            finish();
            return;
        }

        // Fetch Data from Firebase
        fetchWatchlist(username);
    }

    private void fetchWatchlist(String username) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("people").child(username).child("watchlist");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                originalList.clear();
                for (DataSnapshot animeSnapshot : snapshot.getChildren()) {
                    Anime anime = animeSnapshot.getValue(Anime.class);
                    originalList.add(anime);
                }
                adapter = new AnimeAdapterwl(originalList,username);
                recyclerViewWatchlist.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(WatchlistActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAnime(String query) {
        List<Anime> filteredList = new ArrayList<>();
        for (Anime anime : originalList) {
            if (anime.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(anime);
            }
        }
        adapter.updateList(filteredList);
    }

    // Adapter class for RecyclerView
    public static class AnimeAdapterwl extends RecyclerView.Adapter<AnimeAdapterwl.ViewHolderwl> {
        private List<Anime> watchlist;
        private String username;

        public AnimeAdapterwl(List<Anime> watchlist, String username) {
            this.watchlist = watchlist;
            this.username = username;
        }

        @Override
        public ViewHolderwl onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.watchlist_item_layout, parent, false);
            return new ViewHolderwl(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolderwl holder, int position) {
            Anime anime = watchlist.get(position);
            holder.titleTextView.setText(anime.getTitle());
            holder.statusTextView.setText(anime.getStatus());

            Glide.with(holder.itemView.getContext())
                    .load(anime.getImageUrl()) // Assuming this is the URL of the image
                    .placeholder(R.drawable.ic_launcher_background) // Optional placeholder if the image is not found
                    .into(holder.imageView);

            holder.editButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.edit_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.remove_anime) {
                        removeAnimeFromWatchlist(anime, username);
                        return true;
                    } else if (itemId == R.id.change_status) {
                        showChangeStatusDialog(v.getContext(), anime, username);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }

        @Override
        public int getItemCount() {
            return watchlist.size();
        }

        public static class ViewHolderwl extends RecyclerView.ViewHolder {
            TextView titleTextView, statusTextView;
            Button editButton;
            ImageView imageView;

            public ViewHolderwl(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.animeTitle);
                statusTextView = itemView.findViewById(R.id.animeStatus);
                editButton = itemView.findViewById(R.id.editButton);
                imageView = itemView.findViewById(R.id.animeImage);
            }
        }

        public void updateList(List<Anime> newList) {
            this.watchlist = newList;
            notifyDataSetChanged();
        }

        private void removeAnimeFromWatchlist(Anime anime, String username) {
            // Remove anime from Firebase database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("people").child(username).child("watchlist");
            ref.child(String.valueOf(anime.getAnimeId())).removeValue().addOnSuccessListener(aVoid -> {
                // Remove anime from the list in the adapter
                watchlist.remove(anime);
                notifyDataSetChanged();
            });
        }

        private void showChangeStatusDialog(Context context, Anime anime, String username) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Change Status");
            String[] statuses = {"Watching", "Watched", "Plan to Watch", "Dropped"};
            builder.setItems(statuses, (dialog, which) -> {
                anime.setStatus(statuses[which]);
                updateAnimeStatusInDatabase(anime, username);
                notifyDataSetChanged();
            });
            builder.show();
        }

        private void updateAnimeStatusInDatabase(Anime anime, String username) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("people").child(username).child("watchlist")
                    .child(String.valueOf(anime.getAnimeId()));
            ref.child("status").setValue(anime.getStatus())
                    .addOnSuccessListener(aVoid -> {
                        // Notify adapter after the update is successful
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }
}