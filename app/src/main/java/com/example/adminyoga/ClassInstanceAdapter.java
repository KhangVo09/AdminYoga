package com.example.adminyoga;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClassInstanceAdapter extends RecyclerView.Adapter<ClassInstanceAdapter.ViewHolder> {

    private List<ClassInstance> classInstances;
    private OnDeleteClickListener deleteListener;
    private OnEditClickListener editListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(ClassInstance instance);
    }

    public interface OnEditClickListener {
        void onEditClick(ClassInstance instance);
    }

    public ClassInstanceAdapter(List<ClassInstance> classInstances, OnDeleteClickListener deleteListener, OnEditClickListener editListener) {
        this.classInstances = classInstances;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.class_instance_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClassInstance instance = classInstances.get(position);
        holder.tvDate.setText("Date: " + instance.getDate());
        holder.tvTeacher.setText("Teacher: " + instance.getTeacher());
        holder.tvComments.setText("Comments: " + (instance.getComments() != null ? instance.getComments() : ""));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(instance));
        holder.btnEdit.setOnClickListener(v -> editListener.onEditClick(instance));
    }

    @Override
    public int getItemCount() {
        return classInstances.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDate;
        public TextView tvTeacher;
        public TextView tvComments;
        public Button btnDelete;
        public Button btnEdit;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvComments = itemView.findViewById(R.id.tvComments);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}