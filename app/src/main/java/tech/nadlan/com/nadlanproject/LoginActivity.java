package tech.nadlan.com.nadlanproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {




    private DatabaseReference userTable;
    List<User> usersList = new ArrayList<User>();
    EditText userNameEt,passwordEt;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userNameEt = (EditText)findViewById(R.id.username_et);
        passwordEt = (EditText)findViewById(R.id.password_et);
        userTable = FirebaseDatabase.getInstance().getReference("users");
         mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

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

    public void rgisterClicked(View view) {
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }

    public void handleSign(View view) {
        String userName = userNameEt.getText().toString();
        String password = passwordEt.getText().toString();
        if (userName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "אחד או יותר מהשדות חסרים", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.toString().length() < 6) {
            Toast.makeText(this, "סיסמה לא חוקית", Toast.LENGTH_SHORT).show();
            return;
        }
        doSomethingTest(userName,password);

    }

    public void doSomethingTest(String userName, String password){
        final ProgressDialog progress;
        progress = ProgressDialog.show(this, "",
                "מתחבר..", true);
        mAuth.signInWithEmailAndPassword(userName, password)
                .addOnCompleteListener(LoginActivity. this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            progress.dismiss();

                            startActivity(new Intent(LoginActivity.this,MapActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i("FIREBSOSH", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "אחד או יותר מהפרטים אינם נכונים", Toast.LENGTH_SHORT).show();
                            progress.dismiss();

                        }
                    }
                }).addOnFailureListener(LoginActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("FIREBSOSH", "signInWithEmail:failure" + e.getMessage());

                Toast.makeText(LoginActivity.this, "אחד או יותר מהפרטים אינם נכונים", Toast.LENGTH_SHORT).show();
                progress.dismiss();

            }
        });
    }

    private void validateLogin(String userName, String password) {
           }
}
