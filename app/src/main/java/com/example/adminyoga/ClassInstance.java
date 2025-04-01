package com.example.adminyoga;

public class ClassInstance {
    private int id;
    private int courseId;
    private String date;
    private String teacher;
    private String comments;

    public ClassInstance(int id, int courseId, String date, String teacher, String comments) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
    }

    public int getId() { return id; }
    public int getCourseId() { return courseId; }
    public String getDate() { return date; }
    public String getTeacher() { return teacher; }
    public String getComments() { return comments; }
}