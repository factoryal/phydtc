package com.seeun.devsign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static com.seeun.devsign.HomeFragment.dbHelper;
import static com.seeun.devsign.MainActivity.sharedPreference;

public class ModifyDeviceActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    Spinner spinner;
    String[] item;
    Button BTbtn;
    EditText editname;
    static TextView deviceTv2, deviceAddress2;
    ImageButton[] typeImages;
    String selectedType;
    int btnnum, image=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_device);

        //dbHelper = new DBHelper(getApplicationContext(), "DEVICES.db", null, 1);

        btnnum = Integer.parseInt(getIntent().getStringExtra("btnnum"));

        spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        item = new String[]{"선택하세요", "덤벨", "바벨", "상하운동"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        BTbtn = (Button)findViewById(R.id.button);
        BTbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DeviceScanActivity.class);
                startActivity(i);
            }
        });
        typeImages = new ImageButton[6];
        typeImages[0] = (ImageButton)findViewById(R.id.type1red);
        typeImages[1] = (ImageButton) findViewById(R.id.type1blue);
        typeImages[2] = (ImageButton) findViewById(R.id.type2red);
        typeImages[3] = (ImageButton)findViewById(R.id.type2blue);
        typeImages[4] = (ImageButton)findViewById(R.id.type3pink);
        typeImages[5] = (ImageButton)findViewById(R.id.type3yellow);

        deviceTv2 = (TextView)findViewById(R.id.textView2);
        deviceAddress2 = (TextView)findViewById(R.id.textView3);
        editname = (EditText)findViewById(R.id.editText);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(id == 1)
            selectedType = item[1];
        else if(id==2)
            selectedType = item[2];
        else if(id==3)
            selectedType = item[3];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void cancelClick(View view) {
        finish();
    }

    public void redDumbClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[0].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 0;
    }

    public void blueDumbClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[1].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 1;
    }

    public void redBarClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[2].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 2;
    }

    public void blueBarClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[3].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 3;
    }

    public void pinkSquatClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[4].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 4;
    }

    public void yellowSquatClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[5].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 5;

    }

    public void saveClick(View view) {
        String address = deviceAddress2.getText().toString();
        String name = editname.getText().toString();
        if(address == "" || name == "" || selectedType.equals("선택하세요") || image==-1)
            Toast.makeText(getApplicationContext(), "항목을 모두 확인해주세요", Toast.LENGTH_SHORT).show();
        else {
            try {
                dbHelper.insert(btnnum, address, name, selectedType, image);
                Toast.makeText(getApplicationContext(), "등록 완료", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putBoolean("Registered["+btnnum+"]",true);
                editor.commit();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "삽입 오류 발생", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void DeleteClick(View view) {
        dbHelper.delete(btnnum);
        Toast.makeText(getApplicationContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putBoolean("Registered["+btnnum+"]",false);
        editor.commit();
        finish();
    }
}
