package org.androidtown.mobile_term;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class select_course extends AppCompatActivity {

     FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
     DatabaseReference databaseReference = firebaseDatabase.getReference();
     FirebaseUser user;
    public FirebaseAuth mAuth; //Firebase로 로그인한 사용자 정보 알기 위해
    boolean isVisible = false;
    boolean tempCnt = true;

    Intent intent;
    String title;
    int size=0;

    ArrayList<String> backArr = new ArrayList<String>();
    // Intent backIntent;
    ArrayAdapter<String> Adapter;
    Button add;
    Button cancel;
    Button all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_course);

        mAuth = FirebaseAuth.getInstance(); // 인증
        user = mAuth.getCurrentUser(); //현재 로그인한 유저


        intent= getIntent();
        final ArrayList<String> ReceiveArr=intent.getStringArrayListExtra("arr");
        Adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,ReceiveArr);

        // backIntent = new Intent(select_course.this,Gachon_Login.class);
        final ListView listView = (ListView)findViewById(R.id.listview);
        /******/
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(Adapter);



        all=(Button)findViewById(R.id.selectAll);
        add=(Button)findViewById(R.id.add);
        cancel=(Button)findViewById(R.id.cancel);


        add.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                String tr = user.getEmail();
                int get = tr.indexOf("@");
                tr = tr.substring(0,get);
                int pic_num = 0;

                int count;
                count=Adapter.getCount();
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();

                backArr.add("title"+Integer.toString(count+1));
                String title;


                for(int i=count-1;i>=0;i--){
                    if(checkedItems.get(i)){
                        title=ReceiveArr.get(i).toString();
                        databaseReference.child(tr).push().setValue(title+pic_num);
                        pic_num = (pic_num + 1) % 8;
                    }
                }

                Adapter.notifyDataSetChanged();
                finish();

            }
        });

        cancel.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });

        all.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                int count1 = 0;
                count1 = Adapter.getCount();
                int cnt=0;
                int id = all.getId();

             /*
                for (int i = 0; i < count1; i++) {
                    listView.setItemChecked(i, true);
                }*/


                if (tempCnt) {
                    tempCnt = false;

                    Adapter.notifyDataSetChanged();
                    all.setText("전체해제");
                } else {
                    tempCnt = true;

                    Adapter.notifyDataSetChanged();
                    all.setText("전체선택");
                }
                for (int i = 0; i < count1; i++) {
                    if (!tempCnt) {
                        listView.setItemChecked(i, true);
                    } else {
                        listView.setItemChecked(i, false);
                    }
                   // listClick(i);
                }



            }
        });



    }

}


  /*
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tr = user.getEmail();
                int get = tr.indexOf("@");
                tr = tr.substring(0,get);
                int pic_num = 0;

                String title;
                title=ReceiveArr.get(position).toString();

                Log.i("선택값",title+"");


                databaseReference.child(tr).push().setValue(title+pic_num);
                pic_num = (pic_num + 1) % 8;

                Toast.makeText(select_course.this ,title+" 가 추가 되었습니다.",Toast.LENGTH_LONG).show();
            }
         });
         */
