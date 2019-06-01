package org.androidtown.mobile_term;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class BookAdapter extends ArrayAdapter<BookPojo> {
    boolean isVisible;
    Activity context;
    ArrayList<BookPojo> dataList;

    public BookAdapter(Activity context, ArrayList<BookPojo> dataList) {

        super(context, R.layout.book_row, dataList);
        this.context = context;
        this.dataList = dataList;
        this.isVisible = false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(R.layout.book_row, parent, false);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.book_iv_icon);
        TextView name = (TextView) convertView.findViewById(R.id.book_tv_name);
        TextView day = (TextView) convertView.findViewById(R.id.book_last_day);
        TextView size = (TextView) convertView.findViewById(R.id.book_size);
        day.setText(dataList.get(position).getDay());
        size.setText(dataList.get(position).getSize());


        if (dataList.get(position).isFolder()) {
            imageView.setImageResource(R.drawable.folder);
        } else if (dataList.get(position).getName().contains("pdf")) {
            imageView.setImageResource(R.drawable.pdf);
        } else if (dataList.get(position).getName().contains("mp3") || dataList.get(position).getName().contains("m4a") || dataList.get(position).getName().contains("mp4")) {
            imageView.setImageResource(R.drawable.mp3);
        } else if (dataList.get(position).getName().contains("pptx")) {
            imageView.setImageResource(R.drawable.ppt);
        } else if (dataList.get(position).getName().contains("txt")) {
            imageView.setImageResource(R.drawable.txt);
        } else if (dataList.get(position).getName().contains("doc") || dataList.get(position).getName().contains("word") || dataList.get(position).getName().contains("hwp")) {
            imageView.setImageResource(R.drawable.doc);
        } else if (dataList.get(position).getName().contains("jpg")) {
            Glide.with(getContext()).load(dataList.get(position).getLocation() + "/" + dataList.get(position).getName()).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.file);
        }
        if (dataList.get(position).getDay().contains("2019")) {
            day.setText(dataList.get(position).getDay().replace("2019년 ", ""));
        }
        name.setText(dataList.get(position).getName());
        return convertView;
    }
}