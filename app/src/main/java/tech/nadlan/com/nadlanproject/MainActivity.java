package tech.nadlan.com.nadlanproject;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import in.shadowfax.proswipebutton.ProSwipeButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ProSwipeButton proSwipeBtn = (ProSwipeButton) findViewById(R.id.awesome_btn);
        proSwipeBtn.setText("החלק כדי להיכנס");
        proSwipeBtn.setOnSwipeListener(new ProSwipeButton.OnSwipeListener() {
            @Override
            public void onSwipeConfirm() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // task success! show TICK icon in ProSwipeButton
                        proSwipeBtn.showResultIcon(true); // false if task failed
                    }
                }, 500);
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void callUs(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 103);
            return;
        }else {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "0509840407", null)));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "0509840407", null)));
        }
    }
}
