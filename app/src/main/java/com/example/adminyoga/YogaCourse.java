package com.example.adminyoga;

public class YogaCourse {
    private String id;
    private String dayOfWeek;
    private String time;
    private String type;
    private double price;
    private int capacity;
    private int duration;
    private String description;
    private String location;

    // Default constructor (required for Firestore)
    public YogaCourse() {}

    public YogaCourse(String id, String dayOfWeek, String time, String type, double price,
                      int capacity, int duration, String description, String location) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.type = type;
        this.price = price;
        this.capacity = capacity;
        this.duration = duration;
        this.description = description;
        this.location = location;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}