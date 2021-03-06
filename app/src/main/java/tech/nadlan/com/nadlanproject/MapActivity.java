package tech.nadlan.com.nadlanproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shitij.goyal.slidebutton.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static final String TAG = "DASHBOARD_MAP";
    public GoogleMap mMap;
    String[] fruits = {"Apple", "Banana", "Cherry", "Date", "Grape", "Kiwi", "Mango", "Pear"};
    AutoCompleteTextView actv;
    Geocoder geocoder;
    List<Address> lstAdresses;
    public String textSearch = "";
    static final int MAX_RESULT=5;
    public List<RentPoint> pointList;
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    ImageView imageview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        checkPlayServices();
        pointList = new ArrayList<RentPoint>();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        }else
        {
            Log.i("perAsk","in else");
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        mAuth = FirebaseAuth.getInstance();

        imageview = (ImageView)findViewById(R.id.imageview);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    Dialog errorDialog;

    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {

                if (errorDialog == null) {
                    errorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404);
                    errorDialog.setCancelable(false);
                }

                if (!errorDialog.isShowing())
                    errorDialog.show();

            }
        }

        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void uploadImage(View view) {
         if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }else {
             Intent intent = new Intent();
             intent.setType("image/*");
             intent.setAction(Intent.ACTION_GET_CONTENT);
             startActivityForResult(Intent.createChooser(intent, "Select Picture"), 113);

         }
    }
    public FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        final DatabaseReference points = FirebaseDatabase.getInstance().getReference("rent_points").child("emrQ0fT3PuPnCqClvLxEC46ydZc2").getRef();
        points.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                        RentPoint point = userSnapShot.getValue(RentPoint.class);
                        pointList.add(point);
                        Log.i("RentPoint",point.getLat()+"");
                }
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    for(int i=0; i< pointList.size(); i++) {
                        LatLng coordinate = new LatLng(pointList.get(i).getLat(), pointList.get(i).getLon());
                        builder.include(coordinate);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(coordinate)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.apartment_icon))
                                .title(pointList.get(i).getAddress()));

                    }
                LatLngBounds bounds = builder.build();
                final CameraUpdate cu;
                cu = CameraUpdateFactory.newLatLngBounds(bounds, 300);
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {

                        try {
                            mMap.animateCamera(cu, new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {

                                }
                                @Override
                                public void onCancel() {

                                }
                            });
                        }catch (Exception e){

                        }


                    }
                });

                //          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(computeCentroid(coordList), (float)9));
                mMap.setBuildingsEnabled(true);
                mMap.setIndoorEnabled(true);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
    }
    private void signInAnonymously() {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 113 && resultCode == RESULT_OK
                && null != data) {
            if (data == null) {
                return;
            }
            try {
                Uri uri = data.getData();
                final StorageReference filepath = storage.child("Photos").child(uri.getLastPathSegment());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.i(TAG,filepath.getDownloadUrl().getResult().getPath());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageview.setImageBitmap(bitmap);


            } catch (FileNotFoundException e) {
                Log.i(TAG,e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for(RentPoint rp: pointList) {
            if (marker.getPosition().latitude == rp.getLat() && marker.getPosition().longitude == rp.getLon()){

            }

        }
        return true;
    }
}
