package org.androidtown.mobile_term;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static android.view.View.GONE;

public class FolderAdapter extends ArrayAdapter<FilePojo> {
    boolean isVisible;
    Activity context;
    ArrayList<FilePojo> dataList;

    public FolderAdapter(Activity context, ArrayList<FilePojo> dataList) {

        super(context, R.layout.fp_filerow, dataList);
        this.context = context;
        this.dataList = dataList;
        this.isVisible = false;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(R.layout.fp_filerow, parent, false);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.fp_iv_icon);
        TextView name = (TextView) convertView.findViewById(R.id.fp_tv_name);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
        TextView day = (TextView) convertView.findViewById(R.id.fp_last_day);
        TextView size = (TextView) convertView.findViewById(R.id.fp_size);
        day.setText(dataList.get(position).getDay());
        size.setText(dataList.get(position).getSize());

        if (isVisible) {
            checkBox.setVisibility(View.VISIBLE);
        } else checkBox.setVisibility(GONE);

        if (dataList.get(position).isFolder()) {
            imageView.setImageResource(R.drawable.folder);
            checkBox.setVisibility(GONE);
        } else if (dataList.get(position).getName().contains("pdf")) {
            imageView.setImageResource(R.drawable.pdf);
        } else if (dataList.get(position).getName().contains("mp3")) {
            imageView.setImageResource(R.drawable.mp3);
        } else if (dataList.get(position).getName().contains("pptx")) {
            imageView.setImageResource(R.drawable.ppt);
        } else if (dataList.get(position).getName().contains("txt")) {
            imageView.setImageResource(R.drawable.txt);
        } else if (dataList.get(position).getName().contains("zip")) {
            imageView.setImageResource(R.drawable.zip);
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
