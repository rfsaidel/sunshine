package com.example.android.sunshine.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        String value = intent.getStringExtra("VALUE");
        TextView valueText = (TextView) this.findViewById(R.id.value);
        valueText.setText(value);
        Toast.makeText(this,value,Toast.LENGTH_SHORT).show();
    }
}
