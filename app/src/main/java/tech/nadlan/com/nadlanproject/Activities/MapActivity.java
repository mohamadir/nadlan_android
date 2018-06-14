package tech.nadlan.com.nadlanproject.Activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import tech.nadlan.com.nadlanproject.Classes;
import tech.nadlan.com.nadlanproject.R;
import tech.nadlan.com.nadlanproject.Models.RentPoint;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ValueEventListener {
    public static final String TAG = "DASHBOARD_MAP";
    public GoogleMap mMap;
    String[] fruits = {"Apple", "Banana", "Cherry", "Date", "Grape", "Kiwi", "Mango", "Pear"};
    AutoCompleteTextView actv;
    public String textSearch = "";
    Geocoder geocoder;
    List<Address> lstAdresses;
    static final int MAX_RESULT = 5;
    public TextView titleEt, descEt;
    public List<RentPoint> pointList;
    public List<RentPoint> filterPointList;
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    ImageView imageview;
    public static GalleryPhoto galleryPhoto;
    SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        checkPlayServices();

        galleryPhoto = new GalleryPhoto(MapActivity.this);

        pointList = new ArrayList<RentPoint>();
        titleEt = (TextView) findViewById(R.id.details_title_Tv);
        descEt = (TextView) findViewById(R.id.details_desc_Tv);
        searchView = (SearchView) findViewById(R.id.actv_map);
        searchView.setIconifiedByDefault(false);
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.setQueryHint("חפש עיר או כתובת");
        searchView.requestFocus();
        tourGuidIfNotFirstTime();
        //setTourGuide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchView.clearFocus();
            }
        }, 300);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            Log.i("perAsk", "in else");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(Classes.TAG, query);
                mMap.clear();
                updateMapPoints(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")){
                    resetMap();
                }
                return true;
            }
        });
        mAuth = FirebaseAuth.getInstance();

        imageview = (ImageView) findViewById(R.id.imageview);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void tourGuidIfNotFirstTime() {
        SharedPreferences prefs = getSharedPreferences("TourGuide", MODE_PRIVATE);
        Boolean isFirstTime = prefs.getBoolean("firstTime", true);

        if(isFirstTime){
            setTourGuide();
            SharedPreferences.Editor edit = getSharedPreferences("TourGuide", MODE_PRIVATE).edit();
            edit.putBoolean("firstTime", false);
            edit.commit();
        }

    }

    private void setTourGuide() {
        new GuideView.Builder(this)
                .setTitle("הוספת נקודה חדשה")
                .setContentText("ניתן להוסיף דירה או עסק או מגרש")
                .setGravity(GuideView.Gravity.auto) //optional
                .setDismissType(GuideView.DismissType.anywhere) //optional - default GuideView.DismissType.targetView
                .setTargetView(findViewById(R.id.addPointIv))
                .setContentTextSize(12)//optional
                .setTitleTextSize(14)//optional
                .setGuideListener(new GuideView.GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        new GuideView.Builder(MapActivity.this)
                                .setTitle("רשימת נקודות שלי")
                                .setContentText("ניתן לערוך או למחוק את הנקודות שלי")
                                .setGravity(GuideView.Gravity.auto) //optional
                                .setDismissType(GuideView.DismissType.anywhere) //optional - default GuideView.DismissType.targetView
                                .setTargetView(findViewById(R.id.pointsListIv))
                                .setContentTextSize(12)//optional
                                .setGuideListener(new GuideView.GuideListener() {
                                    @Override
                                    public void onDismiss(View view) {
                                        new GuideView.Builder(MapActivity.this)
                                        .setTitle("ניהול קבצים")
                                                .setContentText("דף לניהול מסמכים, כולל העלאות והורדה .")
                                                .setGravity(GuideView.Gravity.auto) //optional
                                                .setDismissType(GuideView.DismissType.anywhere) //optional - default GuideView.DismissType.targetView
                                                .setTargetView(findViewById(R.id.my_filesIv))
                                                .setGuideListener(new GuideView.GuideListener() {
                                                    @Override
                                                    public void onDismiss(View view) {
                                                        new GuideView.Builder(MapActivity.this)
                                                                .setTitle("חיפוש נקודות")
                                                                .setContentText("ניתן לחפש לפי כתובת או עיר")
                                                                .setGravity(GuideView.Gravity.auto) //optional
                                                                .setDismissType(GuideView.DismissType.anywhere) //optional - default GuideView.DismissType.targetView
                                                                .setTargetView(findViewById(R.id.cardView2))
                                                                .setContentTextSize(12)//optional
                                                                .setTitleTextSize(14)//optional
                                                                .setGuideListener(new GuideView.GuideListener() {
                                                                    @Override
                                                                    public void onDismiss(View view) {
                                                                        new GuideView.Builder(MapActivity.this)
                                                                                .setContentText("כעת תוכל להנות מהאפליקציה")
                                                                                .setDismissType(GuideView.DismissType.anywhere) //optional - default GuideView.DismissType.targetView
                                                                                .setTargetView(findViewById(R.id.container))
                                                                                .setGravity(GuideView.Gravity.center)
                                                                                .setContentTextSize(20)//optional
                                                                                .setTitleTextSize(14)//optional
                                                                                .build()
                                                                                .show();
                                                                    }
                                                                })
                                                                .build()
                                                                .show();
                                                    }
                                                })
                                                .setContentTextSize(12)//optional
                                                .setTitleTextSize(14)//optional

                                                .build()
                                                .show();
                                    }
                                })
                                .setTitleTextSize(14)//optional
                                .build()
                                .show();

                        //cardView2
                    }
                })
                .build()
                .show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchView.clearFocus();
            }
        }, 300);
    }

    @Override
    protected void onResume() {
        super.onResume();
    //    points.addValueEventListener(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchView.clearFocus();
            }
        }, 300);
    }

    Dialog errorDialog;

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("STATUS-LIFE","onPause");
        points.removeEventListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updateMapPoints("no-value");
    }

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
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        mMap.setMapStyle(style);
        mMap.setOnMarkerClickListener(this);

    }

    public static final int GALLERY_REQUEST = 22131;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void uploadImage(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
//             Intent intent = new Intent();
//             intent.setType("image/*");
//             intent.setAction(Intent.ACTION_GET_CONTENT);
//             startActivityForResult(Intent.createChooser(intent, "Select Picture"), 113);
            startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);

        }
    }

    public FirebaseAuth mAuth;
    DatabaseReference points;
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("STATUS-LIFE","ONSTART");
        points = FirebaseDatabase.getInstance().getReference("rent_points").child(mAuth.getUid()).getRef();
        points.addValueEventListener(this);

        FirebaseUser user = mAuth.getCurrentUser();


    }




    private void updateMapPoints(String city) {
        Log.i(Classes.TAG,"update map poitns with query: "+city);
        mMap.clear();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (pointList.size() != 0) {
            for (int i = 0; i < pointList.size(); i++) {
                if (city.equals("no-value")) {
                    Log.i(Classes.TAG,"in no-value");

                    LatLng coordinate = new LatLng(pointList.get(i).getLat(), pointList.get(i).getLon());
                    builder.include(coordinate);
                    if(pointList.get(i).getType().equals(Classes.TYPE_APARTMENT)) { // DERA
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(coordinate)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.aparttment))
                                .title(pointList.get(i).getAddress()));
                    }else if (pointList.get(i).getType().equals(Classes.TYPE_LAND)){ // MEGRASH
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(coordinate)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.land_marker))
                                .title(pointList.get(i).getAddress()));
                    }else {                                                         // 3ESEK
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(coordinate)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.store))
                                .title(pointList.get(i).getAddress()));
                    }
                } else {
                    if (
                            !city.equals("no-value") && (city.contains(pointList.get(i).getCity())
                            || city.equals(pointList.get(i).getCity()))
                            || ((city.toLowerCase().indexOf(pointList.get(i).getCity())) != -1)
                            || ((city.toLowerCase().indexOf(pointList.get(i).getCity() +" " + pointList.get(i).getAddress())) != -1)
                            || ((city.toLowerCase().indexOf(pointList.get(i).getAddress())) != -1)
                            || city.equals(pointList.get(i).getAddress())
                            || city.contains(pointList.get(i).getAddress())
                            || pointList.get(i).getAddress().contains(city)
                            || city.contains(pointList.get(i).getCity() +" " + pointList.get(i).getAddress())
                            || (pointList.get(i).getCity() +" " + pointList.get(i).getAddress()).toString().contains(city)
                            ) {
                        Log.i(Classes.TAG,"in not no value");

                        LatLng coordinate = new LatLng(pointList.get(i).getLat(), pointList.get(i).getLon());
                        builder.include(coordinate);
                        if(pointList.get(i).getType().equals(Classes.TYPE_APARTMENT)) { // DERA
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(coordinate)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.aparttment))
                                    .title(pointList.get(i).getAddress()));
                        }else if (pointList.get(i).getType().equals(Classes.TYPE_LAND)){ // MEGRASH
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(coordinate)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.land_marker))
                                    .title(pointList.get(i).getAddress()));
                        }else {                                                         // 3ESEK
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(coordinate)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.store))
                                    .title(pointList.get(i).getAddress()));
                        }
                    }
                }

            }
            try {
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
                        } catch (Exception e) {

                        }


                    }
                });
            } catch (Exception e) {
                resetMap();
            }

        }else {
       //     mMap.animateCamera(CameraUpdateFactory.zoomTo(8));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(	31.771959, 35.217018), 8));


        }

        //          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(computeCentroid(coordList), (float)9));
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
    }


    private void resetMap() {
        Log.i(Classes.TAG,"resset map ");

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (pointList.size() != 0) {
            for (int i = 0; i < pointList.size(); i++) {
                    LatLng coordinate = new LatLng(pointList.get(i).getLat(), pointList.get(i).getLon());
                    builder.include(coordinate);
                if(pointList.get(i).getType().equals(Classes.TYPE_APARTMENT)) { // DERA
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(coordinate)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.aparttment))
                            .title(pointList.get(i).getAddress()));
                }else if (pointList.get(i).getType().equals(Classes.TYPE_LAND)){ // MEGRASH
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(coordinate)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.land_marker))
                            .title(pointList.get(i).getAddress()));
                }else {                                                         // 3ESEK
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(coordinate)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.store))
                            .title(pointList.get(i).getAddress()));
                }
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
                        } catch (Exception e) {

                        }


                    }
                });


        }
        //          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(computeCentroid(coordList), (float)9));
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
    }




    private void signInAnonymously() {

    }

    @Override
    public void onBackPressed() {
        showLeaveDialog();
    }
    public void showLeaveDialog() {

        View closeDialog = LayoutInflater.from(MapActivity.this).inflate(R.layout.close_app_layout, null);
        final Button closeBt = (Button) closeDialog.findViewById(R.id.closeBt);
        final Button cancelBt = (Button) closeDialog.findViewById(R.id.cancelBt);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(closeDialog);

        final android.support.v7.app.AlertDialog dialog4 = builder.create();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK
                && null != data) {
            if (data == null) {
                return;
            }
            try {
               final  Uri uri = data.getData();

                final ProgressDialog progress;
                progress = new ProgressDialog(MapActivity.this);
                progress.setMessage("מעלה תמונה..");

                progress.show();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            galleryPhoto.setPhotoUri(uri);
                            String photoPath = galleryPhoto.getPath();;

                            // Log.i("ResultAccount",photoPath);

                            File f = new File(photoPath);
                            String content_type = getMimeType(f.getPath().replaceAll(" ", "%20"));

                            String file_path = f.getAbsolutePath().replaceAll(" ", "").toString();
                            Log.i("ResultAccount", "myfiles - " + file_path);

                            OkHttpClient client = new OkHttpClient();
                            RequestBody file_body = RequestBody.create(MediaType.parse(content_type), f);

                            RequestBody request_body = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("type", content_type)
                                    .addFormDataPart("single_image", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                                    .build();
                            String id_user2 = "74";

                            Request request = new Request.Builder()
                                    .url("https://dev.snapgroup.co.il/api/upload_single_image/Member/20/gallery")
                                    .post(request_body)
                                    .build();

                            try {
                                Response response = client.newCall(request).execute();
                                String jsonData = response.body().string();
                                JSONObject Jobject = new JSONObject(jsonData);
                                JSONObject imageObj=Jobject.getJSONObject("image");
                                String imageUrl= imageObj.getString("path");

                                Log.i(TAG,"upload image success "+ "https://dev.snapgroup.co.il"+imageUrl);
                                if (!response.isSuccessful()) {
                                    throw new IOException("Error : " + response);
                                }

                                progress.dismiss();

                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.i("ResultAccount", e.getMessage());

                            }
                       /* SharedPreferences.Editor edit = getSharedPreferences("filesRequestSP", MODE_PRIVATE).edit();
                        edit.putString("finish", "Yes");
                        edit.commit();*/
                            //TODO


                        } catch (Exception e) {
                            progress.dismiss();
                            Log.i("ResultAccount", "second catch " + e.getMessage());
                            final String message = e.getMessage().toString().replaceAll(" ", "").replaceAll("\n", "");
                            if (progress != null) {
                                progress.dismiss();
                                MapActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MapActivity.this, "Cannot upload\n Please trye another file!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        finish();
                        startActivity(getIntent());

                    }

                });


                t.start();

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


            } catch (Exception e) {
                Log.i(TAG,e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void uploadImage(final Uri uri){
        final ProgressDialog progress;
        progress = new ProgressDialog(MapActivity.this);
        progress.setMessage("מעלה תמונה..");

        progress.show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    String photoPath = uri.getPath();
                    // Log.i("ResultAccount",photoPath);

                    File f = new File(photoPath);
                    String content_type = getMimeType(f.getPath().replaceAll(" ", "%20"));

                    String file_path = f.getAbsolutePath().replaceAll(" ", "").toString();
                    Log.i("ResultAccount", "myfiles - " + file_path);

                    OkHttpClient client = new OkHttpClient();
                    RequestBody file_body = RequestBody.create(MediaType.parse(content_type), f);

                    RequestBody request_body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type", content_type)
                            .addFormDataPart("file", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                            .build();
                    String id_user2 = "74";

                    Request request = new Request.Builder()
                            .url("https://api.snapgroup.co.il/api/upload/" + id_user2)
                            .post(request_body)
                            .build();

                    try {
                        Response response = client.newCall(request).execute();
                        Log.i(TAG,"upload image success "+response.toString());
                        if (!response.isSuccessful()) {
                            throw new IOException("Error : " + response);
                        }

                        progress.dismiss();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i("ResultAccount", e.getMessage());

                    }
                       /* SharedPreferences.Editor edit = getSharedPreferences("filesRequestSP", MODE_PRIVATE).edit();
                        edit.putString("finish", "Yes");
                        edit.commit();*/
                    //TODO


                } catch (Exception e) {
                    progress.dismiss();
                    Log.i("ResultAccount", "second catch " + e.getMessage());
                    final String message = e.getMessage().toString().replaceAll(" ", "").replaceAll("\n", "");
                    if (progress != null) {
                        progress.dismiss();
                        MapActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MapActivity.this, "Cannot upload\n Please trye another file!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                finish();
                startActivity(getIntent());

            }

        });


        t.start();
    }
    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for(RentPoint rp: pointList) {
            if (marker.getPosition().latitude == rp.getLat() && marker.getPosition().longitude == rp.getLon()){
                findViewById(R.id.pointDetailCard).setVisibility(View.VISIBLE);
                if(rp.getPhotoPath()!=null && !rp.getPhotoPath().equals("")) {
                    Classes.currentRp = rp;
                    Log.i(Classes.TAG, ""+rp.getPhotoPath());
                        titleEt.setText(rp.getType() + " " + rp.getArea() +" מ\"ר" +", "+rp.getEstablishYear());
                        descEt.setText(rp.getAddress() +", "+rp.getCity());
                        Picasso.with(this).load(rp.getPhotoPath()).into(imageview);
                        Log.i(Classes.TAG,  rp.getPhotoPath());

                    return true;
                }
            }

        }
        return true;
    }

    public void addNewPointActivity(View view) {
        findViewById(R.id.pointDetailCard).setVisibility(View.GONE);
        startActivity(new Intent(MapActivity.this,AddRentPointActivity.class));
    }

    public void closeDetails(View view) {
        findViewById(R.id.pointDetailCard).setVisibility(View.GONE);

    }

    public void openDetailsActivity(View view) {
        Intent intent = new Intent(MapActivity.this,PointDetailsActivity.class);
        Pair<View, String> p1 = Pair.create((View)imageview, ViewCompat.getTransitionName(imageview));
        Pair<View, String> p2 = Pair.create((View)titleEt, ViewCompat.getTransitionName(titleEt));
        Pair<View, String> p3 = Pair.create((View)descEt, ViewCompat.getTransitionName(descEt));
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(MapActivity.this, p1, p2, p3);
        intent.putExtra("title",titleEt.getText().toString());
        intent.putExtra("subtitlesubtitle",descEt.getText().toString());
     //   ActivityOptionsCompat options= ActivityOptionsCompat.makeSceneTransitionAnimation(MapActivity.this,imageview, ViewCompat.getTransitionName(imageview));
        findViewById(R.id.pointDetailCard).setVisibility(View.GONE);
        startActivity(intent,options.toBundle());
    }

    public void myApartmentsListActivity(View view) {
        startActivity(new Intent(MapActivity.this,PintsListActivity.class));
    }


    public void showSignOutDialog() {

        View closeDialog = LayoutInflater.from(MapActivity.this).inflate(R.layout.sign_out_layout, null);
        final Button closeBt = (Button) closeDialog.findViewById(R.id.closeBt);
        final Button cancelBt = (Button) closeDialog.findViewById(R.id.cancelBt);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(closeDialog);

        final android.support.v7.app.AlertDialog dialog4 = builder.create();
        dialog4.show();
        closeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog4.dismiss();
                Classes.isLogged = false;
                mAuth.signOut();
                startActivity(new Intent(MapActivity.this,MainActivity.class));
                finish();
            }
        });
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog4.dismiss();
            }
        });

    }


    public void signOutClick(View view) {
        showSignOutDialog();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        pointList.clear();
        for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
            RentPoint point = userSnapShot.getValue(RentPoint.class);
            pointList.add(point);
            Log.i("RentPoint", point.getLat() + "");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateMapPoints("no-value");
            }
        }, 500);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    public void myProfileClick(View view) {
        final View closeDialog = LayoutInflater.from(MapActivity.this).inflate(R.layout.profile_layout, null);
        final TextView userNameTv = (TextView) closeDialog.findViewById(R.id.userNameTv);
        final TextView emailTv = (TextView) closeDialog.findViewById(R.id.emailTv);
        final TextView signoutTv = (TextView) closeDialog.findViewById(R.id.signoutTv);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(closeDialog);

        final android.support.v7.app.AlertDialog dialog4 = builder.create();
        dialog4.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog4.show();
        signoutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignOutDialog();
                dialog4.dismiss();

            }
        });
        emailTv.setText(Classes.currentEmail);
        userNameTv.setText(Classes.currentUser.getUsername());
    }

    public void myFilesClick(View view) {
        startActivity(new Intent(MapActivity.this,MyFilesActivity.class));
    }
}
