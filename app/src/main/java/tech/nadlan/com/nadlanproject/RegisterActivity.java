package tech.nadlan.com.nadlanproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.asksira.dropdownview.DropDownView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    DropDownView countryDd,CityDd;
    List<String> countries = new ArrayList<>();
     FirebaseAuth mAuth;
     List<User> usersList = new ArrayList<User>();
     public EditText emailEt,passwordEt,userNameEt,phoneEt;

    private DatabaseReference userTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        countries.add("ישראל");
        countries.add("ירדן");
        initialViews();
        mAuth = FirebaseAuth.getInstance();
        userTable = FirebaseDatabase.getInstance().getReference("users");


    }

    private void initialViews() {
        userNameEt = (EditText)findViewById(R.id.username_et);
        emailEt = (EditText)findViewById(R.id.email_et);
        passwordEt = (EditText)findViewById(R.id.password_et);
        phoneEt = (EditText)findViewById(R.id.phone_et);
    }

    @Override
    protected void onStart() {
        super.onStart();
        userTable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for( DataSnapshot userSnapShot: dataSnapshot.getChildren()){
                    User user = userSnapShot.getValue(User.class);
                    usersList.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void register(View view) {
        String userName = userNameEt.getText().toString();
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();
        String phone = phoneEt.getText().toString();
        if(userName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() ){
            Toast.makeText(this, "נא למלא את כל השדות חובה", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!canRegister(userName,email))
        {
            Toast.makeText(this, "שם משתמש או דואר אלקטרוני קיימים", Toast.LENGTH_SHORT).show();
            return;
        }
        if ( password.toString().length() < 6){
            Toast.makeText(this, "סיסמה חייבת להכיל לפחות 6 אותיות", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = userTable.push().getKey();
        User user = new User(id,userName,email,password,phone);
        userTable.child(id).setValue(user);
        Classes.currentUser = user;
        successHandle();


    }

    private void successHandle() {
        Toast.makeText(RegisterActivity.this, "נרשמת בהצלחה", Toast.LENGTH_SHORT).show();
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        SharedPreferences.Editor editor = getSharedPreferences("user_pref", MODE_PRIVATE).edit();
        editor.putBoolean("isLogged", true);
        editor.putString("email", Classes.currentUser.email);
        editor.putString("phone", Classes.currentUser.phone);
        editor.putString("username", Classes.currentUser.username);
        editor.putString("id", Classes.currentUser.id);
        editor.commit();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Thread.sleep(1500);
                    RegisterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            onBackPressed();

                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Boolean canRegister(String userName, String email ){
        for(User user : usersList){
            if (user.getEmail().toString().equals(email) || user.getUsername().toString().equals(userName)) return false;
        }
        return true;
    }

}
