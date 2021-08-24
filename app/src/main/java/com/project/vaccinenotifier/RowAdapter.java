package com.project.vaccinenotifier;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.RowViewHolder> {

    private JSONArray array;

    public RowAdapter(JSONArray array) {
        this.array = array;
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_row, parent, false);

        RowViewHolder rvh = new RowViewHolder(v);

        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull RowAdapter.RowViewHolder holder, int position) {
        try {
            JSONObject object = array.getJSONObject(position);

            holder.tvName.setText(object.getString("name"));
            holder.tvAddress.setText(object.getString("address"));
            holder.tvVaccine.setText(object.getString("vaccine"));
            holder.tvTime.setText(object.getString("from") + " - " + object.getString("to"));
            holder.tvMinAge.setText("" + object.getInt("min_age_limit"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return array.length();
    }

    public static class RowViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName, tvAddress, tvVaccine, tvTime, tvMinAge;

        public RowViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvVaccine = itemView.findViewById(R.id.tvVaccine);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMinAge = itemView.findViewById(R.id.tvMinAge);
        }
    }
}
