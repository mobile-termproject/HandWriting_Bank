package org.androidtown.mobile_term;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Book_Dialog extends AppCompatActivity {
    EditText ed1;
    Button save;
    Button cancel;

    int parent_posi = 10;
    int color_picture = 0;

    private FirebaseUser user;
    private FirebaseAuth mAuth; //Firebase로 로그인한 사용자 정보 알기 위해
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private Integer[] colors =
            {R.drawable.purple2,R.drawable.red2, R.drawable.orange2, R.drawable.yellow2, R.drawable.green2, R.drawable.blue2, R.drawable.black2,  R.drawable.pink2};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book__dialog);

        mAuth = FirebaseAuth.getInstance(); // 인증
        user = mAuth.getCurrentUser(); //현재 로그인한 유저

        ed1 = (EditText) findViewById(R.id.type_name);
        save = (Button) findViewById(R.id.saveBt);
        cancel = (Button) findViewById(R.id.cancelBt);

        final GridView colorgridView = (GridView) findViewById(R.id.color_select);
        colorgridView.setAdapter(new Book_Dialog.ImageAdapter(this));
        colorgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                color_picture = colors[position];
                parent_posi = position;
                for (int i = 0; i < parent.getChildCount(); i++) {
                    parent.getChildAt(i).setBackgroundColor(Color.WHITE);
                }
                view.setBackgroundColor(Color.GRAY);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed1.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "책 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (color_picture == 0) {
                    Toast.makeText(getApplicationContext(), "색상을 선택해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    String tr = user.getEmail();
                    int get = tr.indexOf("@");
                    tr = tr.substring(0, get);
                    int datanum = MainActivity.data.size();
                    int chech = 0;

                    if (datanum != 0) {
                        for (int i = 0; i < datanum; i++) {
                            if (MainActivity.data.get(i).getName().equals(ed1.getText().toString())) {
                                Toast.makeText(getApplicationContext(), "이미 존재하는 이름입니다", Toast.LENGTH_SHORT).show();
                                chech = 1;
                            }
                        }
                    }

                    if (chech == 0)
                        databaseReference.child(tr).push().setValue(ed1.getText().toString() + parent_posi);
                    finish();
                }
            }
        });
    }

    public class ImageAdapter extends BaseAdapter {
        public Context context;

        public ImageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return colors.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();

            if (convertView == null) {
                imageView = new ImageView(context);
                param.height = 85;
                param.width = 85;
                param.setGravity(Gravity.CENTER);
                imageView.setLayoutParams(param);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
                convertView = new ImageView(context);
            } else
                imageView = (ImageView) convertView;

            imageView.setImageResource(colors[position]);
            return imageView;
        }
    }
}

