package tech.nadlan.com.nadlanproject.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tech.nadlan.com.nadlanproject.Adapters.PointsListAdapter;
import tech.nadlan.com.nadlanproject.Classes;
import tech.nadlan.com.nadlanproject.R;
import tech.nadlan.com.nadlanproject.RentPoint;

public class PintsListActivity extends AppCompatActivity {
    ListView pointsListView;
    public List<RentPoint> pointList;
    public FirebaseAuth mAuth;
    PointsListAdapter adapter;
    @Override
    protected void onStart() {
        super.onStart();

       final DatabaseReference points = FirebaseDatabase.getInstance().getReference("rent_points").child(mAuth.getUid()).getRef();
     /*    points.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                RentPoint point = dataSnapshot.getValue(RentPoint.class);
                Log.i("OnChiledRemoved","SIZE BEFORE: "+pointList.size());
                for (int i=0;i < pointList.size();i++){
                    if (pointList.get(i).getId().equals(point.getId())){
                        pointList.remove(i);
                        break;
                    }
                }
                Log.i("OnChiledRemoved","SIZE AFTER: "+pointList.size());

                adapter.notifyDataSetChanged();
                pointsListView.setAdapter(adapter);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        points.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pointList.clear();
                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                    RentPoint point = userSnapShot.getValue(RentPoint.class);
                    pointList.add(point);
                }
                adapter = new PointsListAdapter(PintsListActivity.this,pointList);
                pointsListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pints_list);
        mAuth = FirebaseAuth.getInstance();
        pointList = new ArrayList<RentPoint>();

        pointsListView = (ListView) findViewById(R.id.points_listView);
        pointsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showLeaveDialog(position);
            }
        });

    }
    public void showLeaveDialog(final int position) {

        View closeDialog = LayoutInflater.from(PintsListActivity.this).inflate(R.layout.delete_item_layout, null);
        final Button closeBt = (Button) closeDialog.findViewById(R.id.closeBt);
        final Button cancelBt = (Button) closeDialog.findViewById(R.id.cancelBt);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(closeDialog);

        final AlertDialog dialog4 = builder.create();
        dialog4.show();
        closeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Classes.TAG,"from Close");
                FirebaseDatabase.getInstance().getReference().child("rent_points").child(mAuth.getUid()).child(pointList.get(position).getId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(Classes.TAG,"Success");
                        /*pointList.remove(position);
                        PointsListAdapter  mAdapter = new PointsListAdapter(PintsListActivity.this,pointList);
                        pointsListView.setAdapter(mAdapter);
*/
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(Classes.TAG,"Failed - "+ e.getMessage());

                    }
                });;

                Log.i(Classes.TAG,mAuth.getUid() + " - > " +  pointList.get(position).getId());
                dialog4.dismiss();
             //   pointsTable.child()

            }
        });
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Classes.TAG,"from Cancel");

                dialog4.dismiss();
            }
        });

    }
}
