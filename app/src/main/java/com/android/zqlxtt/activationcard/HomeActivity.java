package com.android.zqlxtt.activationcard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void click(View view){

        switch (view.getId()){
            case R.id.button_recycle1:
                Intent intent = new Intent(this,RecyclerActivity1.class);
                startActivity(intent);
                break;
        }

    }
}
