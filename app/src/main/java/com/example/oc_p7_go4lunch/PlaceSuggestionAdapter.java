package com.example.oc_p7_go4lunch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.utils.ItemClickSupport;

import java.util.List;

public class PlaceSuggestionAdapter extends RecyclerView.Adapter<PlaceSuggestionAdapter.ViewHolder> {

    private static List<PlaceSuggestion> placeSuggestions;
    private static OnItemClickListener clickListener;

    public PlaceSuggestionAdapter(List<PlaceSuggestion> placeSuggestions) {
        this.placeSuggestions = placeSuggestions;
    }

    // Déclarer l'interface OnItemClickListener à l'extérieur de la classe PlaceSuggestionAdapter
    public interface OnItemClickListener {
        void onItemClick(PlaceSuggestion suggestion);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_suggestion, parent, false);
        return new ViewHolder(view);
    }

    public static void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.fullText.setText(placeSuggestions.get(position).getFullText());
    }

    @Override
    public int getItemCount() {
        return placeSuggestions.size();
    }

    // Classe ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fullText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fullText = itemView.findViewById(R.id.fullText);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            clickListener.onItemClick(placeSuggestions.get(position));
                        }
                    }
                }
            });
        }
    }
}
