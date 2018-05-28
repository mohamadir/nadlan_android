package tech.nadlan.com.nadlanproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userNameEt = (EditText)findViewById(R.id.username_et);
        passwordEt = (EditText)findViewById(R.id.password_et);
        userTable = FirebaseDatabase.getInstance().getReference("users");

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
        validateLogin(userName,password);

    }

    private void validateLogin(String userName, String password) {
        for(User user : usersList){
            if (user.getUsername().toString().equals(userName) &&  user.getPassword().toString().equals(password))
            {
                Toast.makeText(this, "הכניסה הצליחה", Toast.LENGTH_SHORT).show();
                Classes.currentUser = user;
                Classes.saveToShp(this);
                startActivity(new Intent(LoginActivity.this,MapActivity.class));
                return;
            }
        }
        Toast.makeText(this, "אחד או יותר מהפרטים אינם נכונים, נסה שוב", Toast.LENGTH_SHORT).show();
    }
}
