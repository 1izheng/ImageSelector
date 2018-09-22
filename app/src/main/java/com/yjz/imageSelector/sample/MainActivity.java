package com.yjz.imageSelector.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yjz.imageSelector.ImageSelector;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ImageSelector.builder()
                        .setCrop(true)
                        .setSingleMode(true)
                        .setCropCircle(false)
                        .setWhRatio(0.5f)
                        .start(MainActivity.this, 1);


            }
        });

        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageSelector.builder()
                        .setMaxCount(2)
                        .setSingleMode(false)
                        .start(MainActivity.this, 1);


            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null && requestCode == 1) {
                final List<String> images = data.getStringArrayListExtra(ImageSelector.EXTRA_RESULT);
                final String path = images.get(0);
                Log.d("##########--->", "-----------------" + path);
                Toast.makeText(this, "path:" + images.size(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


