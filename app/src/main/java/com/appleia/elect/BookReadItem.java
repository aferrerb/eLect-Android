package com.appleia.elect;

public class BookReadItem {
    private final String id;
    private final String title;
    private final String author;
    private final String readDate;

    public BookReadItem(String id, String title, String author, String readDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.readDate = readDate;
    }

    public String getId()       { return id; }
    public String getTitle()    { return title; }
    public String getAuthor()   { return author; }
    public String getReadDate() { return readDate; }
}
