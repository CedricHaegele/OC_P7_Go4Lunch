package com.example.oc_p7_go4lunch;

// PlaceSuggestionAdapter.java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaceSuggestionAdapter extends RecyclerView.Adapter<PlaceSuggestionAdapter.ViewHolder> {

    private List<PlaceSuggestion> placeSuggestions;

    public PlaceSuggestionAdapter(List<PlaceSuggestion> placeSuggestions) {
        this.placeSuggestions = placeSuggestions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.fullText.setText(placeSuggestions.get(position).getFullText());
    }

    @Override
    public int getItemCount() {
        return placeSuggestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fullText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fullText = itemView.findViewById(R.id.fullText);
        }
    }
}
