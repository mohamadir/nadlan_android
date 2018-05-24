package tech.nadlan.com.nadlanproject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.asksira.dropdownview.DropDownView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    DropDownView countryDd,CityDd;
    List<String> countries = new ArrayList<>();
     FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        countries.add("ישראל");
        countries.add("ירדן");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void register(View view) {
        Log.d("fb-register", "on register method");

        String id = mDatabase.push().getKey();
        User user = new User(id,"mohamdib","mohmdib@gmail.com","mohamd12345","0509840407");
    mDatabase.child(id).setValue(user);
//        mAuth.createUserWithEmailAndPassword("mohamdib2@gmail.com", "mohamd1234")
//                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(RegisterActivity.this, "Authentication success.",
//                                    Toast.LENGTH_SHORT).show();
//                            // Sign in success, update UI with the signed-in user's information
//
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//
//                        // ...
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d("fb-register","failed");
//            }
//        }).addOnFailureListener(RegisterActivity.this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d("fb-register","failed");
//
//            }
//        });

    }

}
