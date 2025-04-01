package com.example.adminyoga;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "YogaDB";
    private static final int DATABASE_VERSION = 5;
    private FirebaseFirestore firestore;
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_TABLE_YOGACOURSE = "CREATE TABLE YogaCourse (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "dayofweek TEXT, " +
                    "time TEXT, " +
                    "price FLOAT, " +
                    "type TEXT, " +
                    "description TEXT, " +
                    "capacity INTEGER, " +
                    "duration INTEGER, " +
                    "location TEXT)";
            db.execSQL(CREATE_TABLE_YOGACOURSE);

            String CREATE_TABLE_CLASSINSTANCE = "CREATE TABLE ClassInstance (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "courseId INTEGER, " +
                    "date TEXT, " +
                    "teacher TEXT, " +
                    "comments TEXT, " +
                    "FOREIGN KEY(courseId) REFERENCES YogaCourse(Id))";
            db.execSQL(CREATE_TABLE_CLASSINSTANCE);

            Log.d("DatabaseHelper", "Tables created: YogaCourse and ClassInstance");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE YogaCourse ADD COLUMN capacity INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE YogaCourse ADD COLUMN duration INTEGER DEFAULT 0");
                Log.d("DatabaseHelper", "Added capacity and duration columns");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error adding new columns, dropping table instead", e);
                db.execSQL("DROP TABLE IF EXISTS YogaCourse");
                onCreate(db);
                return;
            }
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE YogaCourse ADD COLUMN location TEXT");
                Log.d("DatabaseHelper", "Added location column");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error adding location, dropping table", e);
                db.execSQL("DROP TABLE IF EXISTS YogaCourse");
                onCreate(db);
                return;
            }
        }
        if (oldVersion < 5) {
            try {
                String CREATE_TABLE_CLASSINSTANCE = "CREATE TABLE ClassInstance (" +
                        "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "courseId INTEGER, " +
                        "date TEXT, " +
                        "teacher TEXT, " +
                        "comments TEXT, " +
                        "FOREIGN KEY(courseId) REFERENCES YogaCourse(Id))";
                db.execSQL(CREATE_TABLE_CLASSINSTANCE);
                Log.d("DatabaseHelper", "Created ClassInstance table");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error creating ClassInstance table, dropping all tables", e);
                db.execSQL("DROP TABLE IF EXISTS ClassInstance");
                db.execSQL("DROP TABLE IF EXISTS YogaCourse");
                onCreate(db);
                return;
            }
        }
        Log.d("DatabaseHelper", "Database upgraded to version " + newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ClassInstance");
        db.execSQL("DROP TABLE IF EXISTS YogaCourse");
        onCreate(db);
        Log.d("DatabaseHelper", "Database downgraded from version " + oldVersion + " to " + newVersion);
    }

    public long createNewYogaCourse(String dow, String time, float p, String type, String des, int capacity, int duration, String location) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rowValues = new ContentValues();
        rowValues.put("dayofweek", dow);
        rowValues.put("time", time);
        rowValues.put("price", p);
        rowValues.put("type", type);
        rowValues.put("description", des);
        rowValues.put("capacity", capacity);
        rowValues.put("duration", duration);
        rowValues.put("location", location);
        long id = db.insertOrThrow("YogaCourse", null, rowValues);

        // Sync to cloud
        YogaCourse course = getYogaCourseById((int) id);
        if (course != null) {
            syncCourseToCloud(course);
        }

        db.close();
        return id;
    }

    public Cursor readAllYogaCourse() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.query("YogaCourse",
                new String[] {"Id", "dayofweek", "time", "price", "type", "description", "capacity", "duration", "location"},
                null, null, null, null, null);
        Log.d("DatabaseHelper", "readAllYogaCourse: Returned cursor with count: " + (results != null ? results.getCount() : 0));
        if (results != null && results.getCount() > 0) {
            results.moveToFirst();
        }
        return results; // Caller must close the cursor
    }

    public void deleteYogaCourse(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("YogaCourse", "Id = ?", new String[]{String.valueOf(id)});
        db.delete("ClassInstance", "courseId = ?", new String[]{String.valueOf(id)});

        // Delete from cloud
        if (NetworkUtil.isNetworkAvailable(context)) {
            firestore.collection("yoga_courses")
                    .document(String.valueOf(id))
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("DatabaseHelper", "Course deleted from cloud: " + id))
                    .addOnFailureListener(e -> Log.e("DatabaseHelper", "Error deleting course from cloud: " + id, e));
        }
        db.close();
    }

    public YogaCourse getYogaCourseById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("YogaCourse",
                new String[] {"Id", "dayofweek", "time", "price", "type", "description", "capacity", "duration", "location"},
                "Id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            YogaCourse course = new YogaCourse(
                    String.valueOf(cursor.getInt(cursor.getColumnIndex("Id"))),
                    cursor.getString(cursor.getColumnIndex("dayofweek")),
                    cursor.getString(cursor.getColumnIndex("time")),
                    cursor.getString(cursor.getColumnIndex("type")),
                    cursor.getFloat(cursor.getColumnIndex("price")),
                    cursor.getInt(cursor.getColumnIndex("capacity")),
                    cursor.getInt(cursor.getColumnIndex("duration")),
                    cursor.getString(cursor.getColumnIndex("description")),
                    cursor.getString(cursor.getColumnIndex("location"))
            );
            cursor.close();
            db.close();
            return course;
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return null;
    }

    public void updateYogaCourse(int id, String dow, String time, float price, String type, String des, int capacity, int duration, String location) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rowValues = new ContentValues();
        rowValues.put("dayofweek", dow);
        rowValues.put("time", time);
        rowValues.put("price", price);
        rowValues.put("type", type);
        rowValues.put("description", des);
        rowValues.put("capacity", capacity);
        rowValues.put("duration", duration);
        rowValues.put("location", location);
        db.update("YogaCourse", rowValues, "Id = ?", new String[]{String.valueOf(id)});

        // Sync to cloud
        YogaCourse course = getYogaCourseById(id);
        if (course != null) {
            syncCourseToCloud(course);
        }
        db.close();
    }

    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS ClassInstance");
        db.execSQL("DROP TABLE IF EXISTS YogaCourse");
        onCreate(db);
        db.close();
        Log.d("DatabaseHelper", "Database reset: Dropped and recreated all tables");

        // Clear cloud data
        if (NetworkUtil.isNetworkAvailable(context)) {
            firestore.collection("yoga_courses")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                        Log.d("DatabaseHelper", "All courses deleted from cloud");
                    })
                    .addOnFailureListener(e -> Log.e("DatabaseHelper", "Error deleting courses from cloud", e));

            // Clear class_instances collection (if applicable)
            firestore.collection("class_instances")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                        Log.d("DatabaseHelper", "All class instances deleted from cloud");
                    })
                    .addOnFailureListener(e -> Log.e("DatabaseHelper", "Error deleting class instances from cloud", e));
        }
    }

    public long createClassInstance(int courseId, String date, String teacher, String comments) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rowValues = new ContentValues();
        rowValues.put("courseId", courseId);
        rowValues.put("date", date);
        rowValues.put("teacher", teacher);
        rowValues.put("comments", comments);
        long result = db.insertOrThrow("ClassInstance", null, rowValues);
        Log.d("DatabaseHelper", "Created ClassInstance with courseId: " + courseId);
        db.close();
        return result;
    }

    public Cursor readClassInstances(int courseId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.query("ClassInstance",
                new String[] {"Id", "courseId", "date", "teacher", "comments"},
                "courseId = ?", new String[]{String.valueOf(courseId)}, null, null, null);
        Log.d("DatabaseHelper", "readClassInstances for courseId " + courseId + ": Returned cursor with count: " + (results != null ? results.getCount() : 0));
        if (results != null && results.getCount() > 0) {
            results.moveToFirst();
        }
        return results; // Caller must close the cursor
    }

    public void updateClassInstance(int id, String date, String teacher, String comments) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rowValues = new ContentValues();
        rowValues.put("date", date);
        rowValues.put("teacher", teacher);
        rowValues.put("comments", comments);
        db.update("ClassInstance", rowValues, "Id = ?", new String[]{String.valueOf(id)});
        Log.d("DatabaseHelper", "Updated ClassInstance with id: " + id);
        db.close();
    }

    public void deleteClassInstance(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("ClassInstance", "Id = ?", new String[]{String.valueOf(id)});
        Log.d("DatabaseHelper", "Deleted ClassInstance with id: " + id);
        db.close();
    }

    public void syncCourseToCloud(YogaCourse course) {
        if (NetworkUtil.isNetworkAvailable(context)) {
            firestore.collection("yoga_courses")
                    .document(course.getId())
                    .set(course)
                    .addOnSuccessListener(aVoid -> Log.d("DatabaseHelper", "Course synced to cloud: " + course.getId()))
                    .addOnFailureListener(e -> {
                        Log.e("DatabaseHelper", "Error syncing course to cloud: " + course.getId(), e);
                        Toast.makeText(context, "Error syncing course to cloud: " + course.getId(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Firestore will handle offline writes automatically
            firestore.collection("yoga_courses").document(course.getId()).set(course);
            Toast.makeText(context, "No internet. Course will sync when online.", Toast.LENGTH_SHORT).show();
        }
    }

    public interface SyncCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void syncFromCloud(SyncCallback callback) {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            Toast.makeText(context, "No internet connection. Cannot sync from cloud.", Toast.LENGTH_LONG).show();
            if (callback != null) {
                callback.onFailure(new Exception("No internet connection"));
            }
            return;
        }

        firestore.collection("yoga_courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    SQLiteDatabase db = getWritableDatabase();
                    db.execSQL("DELETE FROM YogaCourse"); // Clear local database
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        YogaCourse course = doc.toObject(YogaCourse.class);
                        ContentValues values = new ContentValues();
                        try {
                            values.put("Id", Integer.parseInt(course.getId()));
                        } catch (NumberFormatException e) {
                            Log.e("DatabaseHelper", "Invalid ID format in Firestore: " + course.getId(), e);
                            continue;
                        }
                        values.put("dayofweek", course.getDayOfWeek());
                        values.put("time", course.getTime());
                        values.put("price", course.getPrice());
                        values.put("type", course.getType());
                        values.put("description", course.getDescription());
                        values.put("capacity", course.getCapacity());
                        values.put("duration", course.getDuration());
                        values.put("location", course.getLocation());
                        db.insert("YogaCourse", null, values);
                    }
                    db.close();
                    Toast.makeText(context, "Synced from cloud", Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseHelper", "Error syncing from cloud", e);
                    Toast.makeText(context, "Error syncing from cloud", Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }
}