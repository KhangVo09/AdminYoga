package com.example.adminyoga;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditClassInstanceActivity extends AppCompatActivity {

    private DatabaseHelper helper;
    private int instanceId;
    private int courseId;
    private String dayOfWeek;
    private EditText editDate, editTeacher, editComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class_instance);
        helper = new DatabaseHelper(this);

        instanceId = getIntent().getIntExtra("instance_id", -1);
        courseId = getIntent().getIntExtra("course_id", -1);
        dayOfWeek = getIntent().getStringExtra("day_of_week");
        if (instanceId == -1 || courseId == -1 || dayOfWeek == null) {
            Toast.makeText(this, "Invalid instance or course", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editDate = findViewById(R.id.editTextDate);
        editTeacher = findViewById(R.id.editTextTeacher);
        editComments = findViewById(R.id.editTextComments);

        // Load existing instance data
        loadInstanceData();

        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                String selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
                String selectedDay = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
                if (!selectedDay.equalsIgnoreCase(dayOfWeek)) {
                    Toast.makeText(this, "Selected date (" + selectedDay + ") does not match course day (" + dayOfWeek + ")", Toast.LENGTH_LONG).show();
                    return;
                }
                editDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void loadInstanceData() {
        Cursor cursor = helper.readClassInstances(courseId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("Id"));
                if (id == instanceId) {
                    editDate.setText(cursor.getString(cursor.getColumnIndex("date")));
                    editTeacher.setText(cursor.getString(cursor.getColumnIndex("teacher")));
                    editComments.setText(cursor.getString(cursor.getColumnIndex("comments")));
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "Failed to load instance data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onClickUpdateInstance(View v) {
        String date = editDate.getText().toString().trim();
        String teacher = editTeacher.getText().toString().trim();
        String comments = editComments.getText().toString().trim();

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (teacher.isEmpty()) {
            Toast.makeText(this, "Please enter a teacher name", Toast.LENGTH_SHORT).show();
            return;
        }

        helper.updateClassInstance(instanceId, date, teacher, comments);
        Toast.makeText(this, "Class instance updated", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK); // Indicate that an update occurred
        finish();
    }
}