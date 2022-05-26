package com.example.oc_p7_go4lunch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class WorkmatesListAdapter extends RecyclerView.Adapter<WorkmatesListAdapter.ViewHolder> {

    private List<UserModel> userList;

    public WorkmatesListAdapter(List<UserModel> userModels) {
        this.userList = userModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_workmate_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel mUser = userList.get(position);
        holder.setData(mUser);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView img;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = (CircleImageView) itemView.findViewById(R.id.imageProfile);
            name = (TextView) itemView.findViewById(R.id.textName);
        }

        private void setData(UserModel mUser) {
            name.setText(mUser.getName());

            Glide.with(img.getContext())
                    .load(mUser.getPhoto())
                    .placeholder(R.drawable.quantum_ic_cloud_off_vd_theme_24)
                    .circleCrop()
                    .error(R.drawable.default_avatar)
                    .into(img);
        }
    }
}
