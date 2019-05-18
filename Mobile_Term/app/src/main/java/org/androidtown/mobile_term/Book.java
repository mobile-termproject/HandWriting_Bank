package org.androidtown.mobile_term;

public class Book {
    private  String name;
    private int imgno;

    public Book (String name, int imgno) {
        this.name = name;
        this.imgno = imgno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImagno() {
        return imgno;
    }

    public void setImgno(int imgno) {
        this.imgno = imgno;
    }
}
