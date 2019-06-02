package org.androidtown.mobile_term;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GridItem extends LinearLayout {
    TextView tx1;
    ImageView img;

    public GridItem(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.gridview_item, this);

        tx1 = (TextView) findViewById(R.id.book_name);
        img = (ImageView) findViewById(R.id.book_img);
    }

    public void setData(Book one) {
        img.setImageResource(one.getImagno());
        tx1.setText(one.getName());
    }
}

