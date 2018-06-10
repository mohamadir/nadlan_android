package tech.nadlan.com.nadlanproject.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import tech.nadlan.com.nadlanproject.R;

public class TestActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    TextView tv;
    public static final  String TAG = "DASHBOARD_MAP";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mAuth = FirebaseAuth.getInstance();
        String errStr = getIntent().getExtras().getString("error");
        tv  = (TextView)findViewById(R.id.errorTv);
        tv.setText(errStr);
        //                final StorageReference filepath = storage.child("Photos").child(uri.getLastPathSegment());
//                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        Toast.makeText(MapActivity.this, "Success upload ", Toast.LENGTH_SHORT).show();
////                        Log.i(TAG,taskSnapshot.getdown);
//                    }
//                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MapActivity.this, "Failed upload "+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//                        Log.i(TAG,e.getMessage().toString());
//
//                    }
//                });
//
//                InputStream inputStream = getContentResolver().openInputStream(data.getData());
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                imageview.setImageBitmap(bitmap);

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}
