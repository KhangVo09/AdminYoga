package com.example.adminyoga;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper helper;
    private RecyclerView recyclerView;
    private YogaCourseAdapter adapter;
    private List<YogaCourse> yogaCourses;
    private List<YogaCourse> filteredCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new DatabaseHelper(this);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerViewCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        yogaCourses = new ArrayList<>();
        filteredCourses = new ArrayList<>();
        adapter = new YogaCourseAdapter(filteredCourses,
                new YogaCourseAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(YogaCourse course) {
                        Log.d("MainActivity", "Course clicked: ID = " + course.getId());
                        Intent intent = new Intent(MainActivity.this, EditYogaCourseActivity.class);
                        intent.putExtra("course_id", course.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(YogaCourse course) {
                        try {
                            int courseId = Integer.parseInt(course.getId());
                            helper.deleteYogaCourse(courseId);
                            loadCourses();
                            Toast.makeText(MainActivity.this, "Course deleted", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Log.e("MainActivity", "Invalid course ID format: " + course.getId(), e);
                            Toast.makeText(MainActivity.this, "Error: Invalid course ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                course -> {
                    try {
                        int courseId = Integer.parseInt(course.getId());
                        Intent intent = new Intent(MainActivity.this, ViewScheduleActivity.class);
                        intent.putExtra("course_id", courseId);
                        intent.putExtra("day_of_week", course.getDayOfWeek());
                        Log.d("MainActivity", "Scheduling course: ID=" + course.getId() + ", Day=" + course.getDayOfWeek());
                        startActivity(intent);
                    } catch (NumberFormatException e) {
                        Log.e("MainActivity", "Invalid course ID format: " + course.getId(), e);
                        Toast.makeText(MainActivity.this, "Error: Invalid course ID", Toast.LENGTH_SHORT).show();
                    }
                });
        recyclerView.setAdapter(adapter);

        // Set up search
        EditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterCourses(s.toString());
            }
        });

        // Sync from cloud on startup and load courses after sync
        helper.syncFromCloud(new DatabaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
                loadCourses();
            }

            @Override
            public void onFailure(Exception e) {
                loadCourses(); // Load local data even if sync fails
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
    }

    private void loadCourses() {
        Cursor cursor = helper.readAllYogaCourse();
        yogaCourses.clear();
        Log.d("MainActivity", "Starting to load courses from cursor, count: " + (cursor != null ? cursor.getCount() : 0));
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"));
                    String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow("dayofweek"));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                    float price = cursor.getFloat(cursor.getColumnIndexOrThrow("price"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    int capacity = cursor.getInt(cursor.getColumnIndexOrThrow("capacity"));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                    YogaCourse course = new YogaCourse(String.valueOf(id), dayOfWeek, time, type, price, capacity, duration, description, location);
                    yogaCourses.add(course);
                    Log.d("MainActivity", "Loaded course: " + id + ", " + dayOfWeek + ", " + time);
                } catch (IllegalArgumentException e) {
                    Log.e("MainActivity", "Column missing in cursor: " + e.getMessage());
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"));
                    String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow("dayofweek"));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                    float price = cursor.getFloat(cursor.getColumnIndexOrThrow("price"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    int capacity = 0;
                    int duration = 0;
                    String location = "";
                    yogaCourses.add(new YogaCourse(String.valueOf(id), dayOfWeek, time, type, price, capacity, duration, description, location));
                }
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.w("MainActivity", "Cursor is null or empty");
            if (cursor != null) cursor.close();
        }
        Log.d("MainActivity", "Total courses loaded: " + yogaCourses.size());
        Toast.makeText(this, "Loaded " + yogaCourses.size() + " courses", Toast.LENGTH_SHORT).show();
        filteredCourses.clear();
        filteredCourses.addAll(yogaCourses);
        adapter.notifyDataSetChanged();
        Log.d("MainActivity", "Adapter notified with " + filteredCourses.size() + " courses");
    }

    private void filterCourses(String query) {
        filteredCourses.clear();
        if (query.isEmpty()) {
            filteredCourses.addAll(yogaCourses);
        } else {
            query = query.toLowerCase();
            for (YogaCourse course : yogaCourses) {
                String dayOfWeek = course.getDayOfWeek() != null ? course.getDayOfWeek().toLowerCase() : "";
                String type = course.getType() != null ? course.getType().toLowerCase() : "";
                if (dayOfWeek.contains(query) || type.contains(query)) {
                    filteredCourses.add(course);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void onCreateYogaCourse(View v) {
        Intent i = new Intent(getApplicationContext(), CreateYogaCourse.class);
        startActivity(i);
    }

    public void onResetDatabase(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Database")
                .setMessage("Are you sure you want to reset the database? All data will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    helper.resetDatabase();
                    loadCourses();
                    Toast.makeText(this, "Database reset", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void onUploadToCloud(View v) {
        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Please connect and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Get all courses from local database
        if (filteredCourses.isEmpty()) {
            Toast.makeText(this, "No courses to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload each course to Firestore using DatabaseHelper
        for (YogaCourse course : filteredCourses) {
            helper.syncCourseToCloud(course);
        }

        Toast.makeText(this, "Uploading " + filteredCourses.size() + " courses to cloud", Toast.LENGTH_SHORT).show();
    }
}