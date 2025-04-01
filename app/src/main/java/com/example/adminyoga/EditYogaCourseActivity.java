package com.example.adminyoga;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditYogaCourseActivity extends AppCompatActivity {

    private DatabaseHelper helper;
    private YogaCourse course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_yoga);
        helper = new DatabaseHelper(this);

        // Load the course to edit
        int courseId = getIntent().getIntExtra("course_id", -1);
        Log.d("EditYogaCourse", "Received course ID: " + courseId);
        if (courseId == -1) {
            Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Find the course in the database
        course = helper.getYogaCourseById(courseId);
        if (course == null) {
            Log.e("EditYogaCourse", "Course not found for ID: " + courseId);
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate Day of Week Spinner
        Spinner spDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        if (spDayOfWeek == null) {
            Log.e("EditYogaCourse", "Spinner spinnerDayOfWeek not found in layout");
            Toast.makeText(this, "Error: Day of Week spinner not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDayOfWeek.setAdapter(dayAdapter);
        spDayOfWeek.setSelection(dayAdapter.getPosition(course.getDayOfWeek()));

        // Populate Time Spinner
        Spinner spTime = findViewById(R.id.spinnerTime);
        if (spTime == null) {
            Log.e("EditYogaCourse", "Spinner spinnerTime not found in layout");
            Toast.makeText(this, "Error: Time spinner not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_slots, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTime.setAdapter(timeAdapter);
        spTime.setSelection(timeAdapter.getPosition(course.getTime()));

        // Populate Type Spinner
        Spinner spType = findViewById(R.id.spinnerType);
        if (spType == null) {
            Log.e("EditYogaCourse", "Spinner spinnerType not found in layout");
            Toast.makeText(this, "Error: Type spinner not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.yoga_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);
        spType.setSelection(typeAdapter.getPosition(course.getType()));

        // Populate other fields
        EditText editPrice = findViewById(R.id.editTextPrice);
        EditText editCapacity = findViewById(R.id.editTextCapacity);
        EditText editDuration = findViewById(R.id.editTextDuration);
        EditText editDescription = findViewById(R.id.editTextDescription);
        EditText editLocation = findViewById(R.id.editTextLocation);

        if (editPrice == null || editCapacity == null || editDuration == null || editDescription == null || editLocation == null) {
            Log.e("EditYogaCourse", "One or more EditText fields not found in layout");
            Toast.makeText(this, "Error: Some fields are missing in the layout", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        editPrice.setText(String.valueOf(course.getPrice()));
        editCapacity.setText(String.valueOf(course.getCapacity()));
        editDuration.setText(String.valueOf(course.getDuration()));
        editDescription.setText(course.getDescription() != null ? course.getDescription() : "");
        editLocation.setText(course.getLocation() != null ? course.getLocation() : "");
    }

    public void onClickUpdateYogaCourse(View v) {
        try {
            String dayOfWeek, time, type, des, location;
            float price;
            int capacity, duration;

            Spinner spDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
            Spinner spTime = findViewById(R.id.spinnerTime);
            Spinner spType = findViewById(R.id.spinnerType);
            EditText editDes = findViewById(R.id.editTextDescription);
            EditText editPrice = findViewById(R.id.editTextPrice);
            EditText editCapacity = findViewById(R.id.editTextCapacity);
            EditText editDuration = findViewById(R.id.editTextDuration);
            EditText editLocation = findViewById(R.id.editTextLocation);

            // Validate required fields
            if (spDayOfWeek == null || spDayOfWeek.getSelectedItem() == null || spDayOfWeek.getSelectedItem().toString().isEmpty()) {
                Toast.makeText(this, "Please select a day of the week", Toast.LENGTH_SHORT).show();
                return;
            }
            dayOfWeek = spDayOfWeek.getSelectedItem().toString();

            if (spTime == null || spTime.getSelectedItem() == null || spTime.getSelectedItem().toString().isEmpty()) {
                Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }
            time = spTime.getSelectedItem().toString();

            if (spType == null || spType.getSelectedItem() == null || spType.getSelectedItem().toString().isEmpty()) {
                Toast.makeText(this, "Please select a type of class", Toast.LENGTH_SHORT).show();
                return;
            }
            type = spType.getSelectedItem().toString();

            des = editDes != null ? editDes.getText().toString() : "";
            location = editLocation != null ? editLocation.getText().toString() : "";

            try {
                if (editPrice == null || editPrice.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show();
                    return;
                }
                price = Float.parseFloat(editPrice.getText().toString());

                if (editCapacity == null || editCapacity.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Please enter a capacity", Toast.LENGTH_SHORT).show();
                    return;
                }
                capacity = Integer.parseInt(editCapacity.getText().toString());

                if (editDuration == null || editDuration.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Please enter a duration", Toast.LENGTH_SHORT).show();
                    return;
                }
                duration = Integer.parseInt(editDuration.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid numeric input (Price, Capacity, or Duration)", Toast.LENGTH_SHORT).show();
                return;
            }

            helper.updateYogaCourse(Integer.parseInt(course.getId()), dayOfWeek, time, price, type, des, capacity, duration, location);
            Toast.makeText(this, "Course updated", Toast.LENGTH_LONG).show();
            finish();
        } catch (Exception e) {
            Log.e("EditYogaCourse", "Error updating course", e);
            Toast.makeText(this, "Error updating course: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}