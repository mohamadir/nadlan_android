package tech.nadlan.com.nadlanproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import in.shadowfax.proswipebutton.ProSwipeButton;

public class MainActivity extends AppCompatActivity {


    TextView userNameTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ProSwipeButton proSwipeBtn = (ProSwipeButton) findViewById(R.id.awesome_btn);
        userNameTv = (TextView)findViewById(R.id.userNameTv);
        setUserName();
        proSwipeBtn.setText("החלק כדי להיכנס");
        proSwipeBtn.showResultIcon(false);
        setTimeMessage();
        proSwipeBtn.setOnSwipeListener(new ProSwipeButton.OnSwipeListener() {
            @Override
            public void onSwipeConfirm() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // task success! show TICK icon in ProSwipeButton
                        proSwipeBtn.showResultIcon(false); // false if task failed
                    }
                }, 500);
                proSwipeBtn.showResultIcon(false);
                if(Classes.isLogged){
                    startActivity(new Intent(MainActivity.this,MapActivity.class));

                }
               else
                   startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
    }

    private void setUserName() {
        if(Classes.isLogged){
            userNameTv.setText(Classes.currentUser.username);
        }else {
            userNameTv.setText("משתמש יקר");

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserName();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setUserName();

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

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        showLeaveDialog();
    }

    public void setTimeMessage(){
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            Toast.makeText(this, "Good Morning", Toast.LENGTH_SHORT).show();
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            Toast.makeText(this, "Good Afternoon", Toast.LENGTH_SHORT).show();
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            Toast.makeText(this, "Good Evening", Toast.LENGTH_SHORT).show();
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            Toast.makeText(this, "Good Night", Toast.LENGTH_SHORT).show();
        }
    }

    public void showLeaveDialog() {

        View closeDialog = LayoutInflater.from(MainActivity.this).inflate(R.layout.close_app_layout, null);
        final Button closeBt = (Button) closeDialog.findViewById(R.id.closeBt);
        final Button cancelBt = (Button) closeDialog.findViewById(R.id.cancelBt);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(closeDialog);

        final AlertDialog dialog4 = builder.create();
        dialog4.show();
        closeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog4.dismiss();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog4.dismiss();
            }
        });

    }

    public void testClick(View view) {
        startActivity(new Intent(MainActivity.this,TestActivity.class));
    }
}
