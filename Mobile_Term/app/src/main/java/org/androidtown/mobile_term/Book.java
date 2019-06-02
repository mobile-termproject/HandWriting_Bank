package org.androidtown.mobile_term;

public class Book {
    private  String name;
    private int imgno;
    private  int picnum;

    public Book (String name, int imgno, int picnum) {
        this.name = name;
        this.imgno = imgno;
        this.picnum = picnum;
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

    public int getPicnum() {
        return picnum;
    }

    public void setPicnum(int picnum) {
        this.picnum = picnum;
    }
}
