package org.androidtown.mobile_term;

public class BookPojo {
    private String name;
    private String day;
    private boolean folder;
    private String size;

    public BookPojo(String name, boolean folder) {
        this.name = name;
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDay() {
        return day;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

}
