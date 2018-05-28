package tech.nadlan.com.nadlanproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences prefs = getSharedPreferences("user_pref", MODE_PRIVATE);
                    Boolean isLogged = prefs.getBoolean("isLogged", false);
                    if(isLogged){

                        String email =   prefs.getString("email", "no-value");
                        String phone =   prefs.getString("phone","no-value");
                        String username =     prefs.getString("username", "no-value");
                        String id =     prefs.getString("id", "-1");
                        Classes.currentUser = new User(id,username,email,"",phone);
                        Classes.isLogged = true;
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
            }
        }).start();
    }
}
