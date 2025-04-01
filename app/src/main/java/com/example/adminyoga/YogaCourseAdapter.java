package com.example.adminyoga;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class YogaCourseAdapter extends RecyclerView.Adapter<YogaCourseAdapter.YogaCourseViewHolder> {

    private List<YogaCourse> yogaCourses;
    private OnItemClickListener listener;
    private OnScheduleClickListener scheduleListener;

    public interface OnItemClickListener {
        void onItemClick(YogaCourse course);
        void onDeleteClick(YogaCourse course);
    }

    public interface OnScheduleClickListener {
        void onScheduleClick(YogaCourse course);
    }

    public YogaCourseAdapter(List<YogaCourse> yogaCourses, OnItemClickListener listener, OnScheduleClickListener scheduleListener) {
        this.yogaCourses = yogaCourses;
        this.listener = listener;
        this.scheduleListener = scheduleListener;
    }

    @NonNull
    @Override
    public YogaCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.yoga_course_item, parent, false);
        return new YogaCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YogaCourseViewHolder holder, int position) {
        YogaCourse course = yogaCourses.get(position);
        holder.tvDay.setText("Day: " + course.getDayOfWeek());
        holder.tvTime.setText("Time: " + course.getTime());
        holder.tvType.setText("Type: " + course.getType());
        holder.tvPrice.setText("Price: $" + course.getPrice());
        holder.tvCapacity.setText("Capacity: " + course.getCapacity());
        holder.tvDuration.setText("Duration: " + course.getDuration() + " min");
        holder.tvLocation.setText("Location: " + (course.getLocation() != null ? course.getLocation() : "N/A"));
        holder.tvDescription.setText("Description: " + (course.getDescription() != null ? course.getDescription() : "N/A"));

        holder.btnEdit.setOnClickListener(v -> listener.onItemClick(course));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(course));
        holder.btnSchedule.setOnClickListener(v -> scheduleListener.onScheduleClick(course));
    }

    @Override
    public int getItemCount() {
        return yogaCourses.size();
    }

    public static class YogaCourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTime, tvType, tvPrice, tvCapacity, tvDuration, tvLocation, tvDescription;
        Button btnEdit, btnDelete, btnSchedule;

        public YogaCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvType = itemView.findViewById(R.id.tvType);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSchedule = itemView.findViewById(R.id.btnSchedule);
        }
    }
}