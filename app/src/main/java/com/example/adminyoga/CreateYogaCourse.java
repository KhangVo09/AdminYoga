package com.example.adminyoga;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateYogaCourse extends AppCompatActivity {

    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_yoga);
        helper = new DatabaseHelper(this);

        // Populate Day of Week Spinner
        Spinner spDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDayOfWeek.setAdapter(dayAdapter);

        // Populate Time Spinner
        Spinner spTime = findViewById(R.id.spinnerTime);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_slots, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTime.setAdapter(timeAdapter);

        // Populate Type Spinner
        Spinner spType = findViewById(R.id.spinnerType);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.yoga_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);
    }

    public void onClickCreateYogaCourse(View v) {
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

        des = editDes != null ? editDes.getText().toString() : ""; // Optional field
        location = editLocation != null ? editLocation.getText().toString() : ""; // Optional field

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

        String details = "Day: " + dayOfWeek + "\nTime: " + time + "\nPrice: $" + price + "\nType: " + type +
                "\nCapacity: " + capacity + "\nDuration: " + duration + " min" +
                (des.isEmpty() ? "" : "\nDescription: " + des) +
                (location.isEmpty() ? "" : "\nLocation: " + location);
        new AlertDialog.Builder(this)
                .setTitle("Confirm Yoga Course")
                .setMessage(details)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    helper.createNewYogaCourse(dayOfWeek, time, price, type, des, capacity, duration, location);
                    Toast.makeText(this, "A yoga class is just created", Toast.LENGTH_LONG).show();
                    finish();
                })
                .setNegativeButton("Edit", null)
                .show();
    }

    public void onClickClear(View v) {
        Spinner spDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        Spinner spTime = findViewById(R.id.spinnerTime);
        Spinner spType = findViewById(R.id.spinnerType);
        EditText editDes = findViewById(R.id.editTextDescription);
        EditText editPrice = findViewById(R.id.editTextPrice);
        EditText editCapacity = findViewById(R.id.editTextCapacity);
        EditText editDuration = findViewById(R.id.editTextDuration);
        EditText editLocation = findViewById(R.id.editTextLocation);

        if (spDayOfWeek != null) spDayOfWeek.setSelection(0);
        if (spTime != null) spTime.setSelection(0);
        if (spType != null) spType.setSelection(0);
        if (editDes != null) editDes.setText("");
        if (editPrice != null) editPrice.setText("");
        if (editCapacity != null) editCapacity.setText("");
        if (editDuration != null) editDuration.setText("");
        if (editLocation != null) editLocation.setText("");
    }
}