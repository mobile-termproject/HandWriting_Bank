package org.androidtown.mobile_term;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class GridViewAdapter extends BaseAdapter {
    ArrayList<Book> book;
    Context context;

    public GridViewAdapter(ArrayList<Book> book, Context context) {
        this.book = book;
        this.context = context;
    }

    @Override
    public  int getCount(){
        return book.size();
    }

    @Override
    public Object getItem(int position) {
        return book.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = new GridItem(context);

        ((GridItem)convertView).setData(book.get(position));

        return convertView;
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    public void addBook(Book one) {
        book.add(one);
        refresh();
    }

    public void remove(int position) {
        book.remove(position);
        refresh();
    }

    public void clear() {
        book.clear();
    }
}
