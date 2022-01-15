package com.example.sandglass;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String tag = new String("Sandglass a1");
    private EditText time_input = null;
    private TextView time_remain = null;
    private Button btn_start = null;
    private Button btn_cancel = null;

    private void initUI() {
        time_input = findViewById(R.id.time_input);
        time_remain = findViewById(R.id.time_remain);
        btn_start = findViewById(R.id.btn_start);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(tag, "Call onCreate");
        setContentView(R.layout.activity_main);
        initUI();
        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(tag, "Call onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(tag, "Call onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(tag, "Call onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(tag, "Call onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(tag, "Call onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(tag, "Call onDestroy");
    }
}