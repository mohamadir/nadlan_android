package tech.nadlan.com.nadlanproject.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.asksira.dropdownview.DropDownView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import tech.nadlan.com.nadlanproject.Classes;
import tech.nadlan.com.nadlanproject.R;
import tech.nadlan.com.nadlanproject.Models.User;

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
        FirebaseUser currentUser = mAuth.getCurrentUser();


    }

    public void register(View view) {
        final String userName = userNameEt.getText().toString();
        final String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();
        final String phone = phoneEt.getText().toString();
        if(userName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() ){
            Toast.makeText(this, "נא למלא את כל השדות חובה", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            User user = new User(
                                    userName,
                                    phone
                            );
                            Classes.currentUser = user;
                            Classes.currentEmail = email;
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        successHandle();
                                    } else {
                                        //display a failure message
                                        Toast.makeText(RegisterActivity.this,"SUCCESS FAILED ", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this,"SUCCESS FAILED", Toast.LENGTH_LONG).show();
                        }
                    }
                });



//        String id = userTable.push().getKey();
//        User user = new User(id,userName,email,password,phone);
//        userTable.child(id).setValue(user);
//        Classes.currentUser = user;
//        successHandle();


    }

    private void successHandle() {
        Toast.makeText(RegisterActivity.this, "נרשמת בהצלחה", Toast.LENGTH_SHORT).show();
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        Classes.saveToShp(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Thread.sleep(1500);
                    RegisterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(RegisterActivity.this,MapActivity.class));
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Boolean canRegister(String userName, String email ){

        return true;
    }

}
