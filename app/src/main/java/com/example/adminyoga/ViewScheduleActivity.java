package com.example.adminyoga;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ViewScheduleActivity extends AppCompatActivity {

    private DatabaseHelper helper;
    private int courseId;
    private String dayOfWeek;
    private RecyclerView recyclerView;
    private ClassInstanceAdapter adapter;
    private List<ClassInstance> classInstances; // Full list
    private List<ClassInstance> filteredClassInstances; // Filtered list for display
    private static final int REQUEST_CODE_EDIT = 1;
    private String selectedDate; // To store the selected date for adding instances
    private Spinner spinnerSearchType;
    private EditText editTextSearch;
    private String currentSearchType = "Date"; // Default search type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule);
        helper = new DatabaseHelper(this);

        courseId = getIntent().getIntExtra("course_id", -1);
        dayOfWeek = getIntent().getStringExtra("day_of_week");
        if (courseId == -1 || dayOfWeek == null) {
            Toast.makeText(this, "Invalid course or day", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Schedule for " + dayOfWeek + " Course");

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        recyclerView = findViewById(R.id.recyclerViewInstances);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classInstances = new ArrayList<>();
        filteredClassInstances = new ArrayList<>();
        adapter = new ClassInstanceAdapter(filteredClassInstances,
                instance -> {
                    helper.deleteClassInstance(instance.getId());
                    loadInstances();
                    Toast.makeText(this, "Instance deleted", Toast.LENGTH_SHORT).show();
                },
                instance -> {
                    Intent intent = new Intent(ViewScheduleActivity.this, EditClassInstanceActivity.class);
                    intent.putExtra("instance_id", instance.getId());
                    intent.putExtra("course_id", courseId);
                    intent.putExtra("day_of_week", dayOfWeek);
                    startActivityForResult(intent, REQUEST_CODE_EDIT);
                });
        recyclerView.setAdapter(adapter);

        // Set up the Spinner for search type
        spinnerSearchType = findViewById(R.id.spinnerSearchType);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.search_types,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchType.setAdapter(spinnerAdapter);
        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSearchType = parent.getItemAtPosition(position).toString();
                editTextSearch.setText(""); // Clear the search field when changing search type
                editTextSearch.setHint(getSearchHint());
                editTextSearch.setFocusable(true);
                editTextSearch.setFocusableInTouchMode(true);
                if (currentSearchType.equals("Date")) {
                    editTextSearch.setFocusable(false);
                    editTextSearch.setFocusableInTouchMode(false);
                }
                filterInstances(""); // Reset the filter when changing search type
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentSearchType = "Date"; // Default to Date if nothing is selected
            }
        });

        // Set up the search EditText
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.setHint(getSearchHint());
        editTextSearch.setOnClickListener(v -> {
            if (currentSearchType.equals("Date")) {
                showSearchDatePickerDialog();
            }
        });
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!currentSearchType.equals("Date")) {
                    filterInstances(s.toString());
                }
            }
        });

        loadInstances();
    }

    private String getSearchHint() {
        switch (currentSearchType) {
            case "Date":
                return "Select date (dd/MM/yyyy)";
            case "Teacher":
                return "Enter teacher name";
            case "Comments":
                return "Enter comments";
            default:
                return "Enter search query";
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
            String selectedDay = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
            if (!selectedDay.equalsIgnoreCase(dayOfWeek)) {
                Toast.makeText(this, "Selected date (" + selectedDay + ") does not match course day (" + dayOfWeek + ")", Toast.LENGTH_LONG).show();
                selectedDate = null; // Reset if the day doesn't match
                return;
            }
            Toast.makeText(this, "Date selected: " + selectedDate, Toast.LENGTH_SHORT).show(); // Feedback to user
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showSearchDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String searchDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
            editTextSearch.setText(searchDate);
            filterInstances(searchDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void onClickAddInstance(View v) {
        EditText editTeacher = findViewById(R.id.editTextTeacher);
        EditText editComments = findViewById(R.id.editTextComments);

        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacher = editTeacher.getText().toString().trim();
        String comments = editComments.getText().toString().trim();

        if (teacher.isEmpty()) {
            Toast.makeText(this, "Please enter a teacher name", Toast.LENGTH_SHORT).show();
            return;
        }

        helper.createClassInstance(courseId, selectedDate, teacher, comments);
        Toast.makeText(this, "Class instance added", Toast.LENGTH_SHORT).show();
        loadInstances();

        // Reset fields after adding
        selectedDate = null;
        editTeacher.setText("");
        editComments.setText("");
    }

    private void loadInstances() {
        Cursor cursor = helper.readClassInstances(courseId);
        classInstances.clear();
        filteredClassInstances.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("Id"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String teacher = cursor.getString(cursor.getColumnIndex("teacher"));
                String comments = cursor.getString(cursor.getColumnIndex("comments"));
                ClassInstance instance = new ClassInstance(id, courseId, date, teacher, comments);
                classInstances.add(instance);
                filteredClassInstances.add(instance); // Initially, filtered list is the same as full list
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void filterInstances(String searchText) {
        filteredClassInstances.clear();
        if (searchText.isEmpty()) {
            filteredClassInstances.addAll(classInstances); // If search is empty, show all instances
        } else {
            for (ClassInstance instance : classInstances) {
                switch (currentSearchType) {
                    case "Date":
                        if (instance.getDate().equalsIgnoreCase(searchText)) {
                            filteredClassInstances.add(instance);
                        }
                        break;
                    case "Teacher":
                        if (instance.getTeacher().toLowerCase().contains(searchText.toLowerCase())) {
                            filteredClassInstances.add(instance);
                        }
                        break;
                    case "Comments":
                        if (instance.getComments() != null && instance.getComments().toLowerCase().contains(searchText.toLowerCase())) {
                            filteredClassInstances.add(instance);
                        }
                        break;
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK) {
            loadInstances(); // Refresh the list after editing
        }
    }
}