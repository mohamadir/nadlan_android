package tech.nadlan.com.nadlanproject.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kosalgeek.android.photoutil.GalleryPhoto;

import org.angmarch.views.NiceSpinner;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.nadlan.com.nadlanproject.*;
import tech.nadlan.com.nadlanproject.Models.RentPoint;
import tech.nadlan.com.nadlanproject.R;

public class AddRentPointActivity extends AppCompatActivity {
    public NiceSpinner typeSpinner;
    public static GalleryPhoto galleryPhoto;
    public Intent ImageData = null;
    public String currentType = "דירה";
    public EditText phoneEt,cityEt,addressEt,contactEt,establishYearEt,descriptionEt,areaEt;
    public String[] types =  new String[]{"דירה", "מגרש" , "עסק"};
    public ImageView pointImage;
    public FirebaseAuth mAuth;
    Double lat,lon;
    private DatabaseReference pointsTable = FirebaseDatabase.getInstance().getReference("rent_points");
    public String imagePath = "";
    Geocoder geocoder;
    List<Address> lstAdresses;
    RadioButton manualRb,autoRb;
    static final int MAX_RESULT = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(tech.nadlan.com.nadlanproject.R.layout.activity_add_rent_point);
        typeSpinner = (NiceSpinner) findViewById(R.id.typeSpinner);
        manualRb = (RadioButton)findViewById(R.id.manualRb);
        autoRb = (RadioButton)findViewById(R.id.autoRb);
        galleryPhoto = new GalleryPhoto(AddRentPointActivity.this);
        initializeViews();
        setGps();
        setRadioButtons();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddRentPointActivity.this,
                android.R.layout.simple_spinner_item,types);
        mAuth = FirebaseAuth.getInstance();

        typeSpinner.setAdapter(adapter);
        typeSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentType = types[position];
            }
        });

    }

    private void setRadioButtons() {
        manualRb.setChecked(false);
        autoRb.setChecked(true);

        autoRb.setChecked(true);
        manualRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoRb.setChecked(false);
                manualRb.setChecked(true);
                cityEt.setText("");
                addressEt.setText("");
            }
        });

        autoRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                manualRb.setChecked(false);
                autoRb.setChecked(true);
                setGps();
            }
        });
    }

    private void setGps() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            setCurrentLocation();
            Geocoder geocoderr;
            List<Address> addressess;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addressess = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addressess.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addressess.get(0).getLocality();
                Log.i("GPSLOC-","city:"+city+",  address: "+address);
                cityEt.setText(city);
                addressEt.setText(address);


            } catch (IOException e) {
                e.printStackTrace();
                Log.i("GPSLOC-","fAILED to get address");

            }



        } else {

            showGPSDisabledAlertToUser();
        }
    }

    private void initializeViews() {
        phoneEt = (EditText)findViewById(R.id.phoneEt);
        contactEt = (EditText)findViewById(R.id.contactEt);
        addressEt = (EditText)findViewById(R.id.addressEt);
        cityEt = (EditText)findViewById(R.id.cityEt);
        descriptionEt = (EditText)findViewById(R.id.desctiptionEt);
        areaEt = (EditText)findViewById(R.id.areaEt);
        pointImage = (ImageView)findViewById(R.id.pointImage);
        establishYearEt = (EditText)findViewById(R.id.establishYear);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void pickImage(View view){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(galleryPhoto.openGalleryIntent(), Classes.GALLERY_REQUEST);

        }else
        {
            Log.i("perAsk","in else");
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i=0;i<grantResults.length;i++){
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) return;
        }
        startActivityForResult(galleryPhoto.openGalleryIntent(), Classes.GALLERY_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==  1){
            setGps();
            Toast.makeText(this, "gps is " + lat + ", "+ lon, Toast.LENGTH_SHORT).show();
        }
        if (requestCode == Classes.GALLERY_REQUEST && resultCode == RESULT_OK
                && null != data) {
            if (data == null) {
                return;
            } else {
                ImageData = data;
                Uri uri = ImageData.getData();
                galleryPhoto.setPhotoUri(uri);
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(data.getData());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                pointImage.setImageBitmap(bitmap);
                ImageData = data;
            }
        }
    }

    private void setCurrentLocation() {
        GPSTracker gps = new GPSTracker(this);
        lat = gps.getLatitude();
        lon = gps.getLongitude();
        uploadImage();

    }

    public   ProgressDialog progress;

    public void uploadImage(){
        progress = new ProgressDialog(AddRentPointActivity.this);;
        progress.setMessage("מעלה תמונה..");
        if (ImageData == null) {
            return;
        }
        try {
            progress.show();
            final Uri uri = ImageData.getData();
            final StorageReference sotrage = FirebaseStorage.getInstance().getReference();
            final Date currentTime = Calendar.getInstance().getTime();
            final StorageReference filepath = sotrage.child("Photos").child(currentTime.toString() + uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    sotrage.child("Photos/"+currentTime.toString()+uri.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            try {
                                final String path =  new URL(uri.toString()).toString();
                                setPointToList(path);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                if(progress.isShowing())
                                    progress.dismiss();
                            }
                            /// The string(file link) that you need
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            Toast.makeText(AddRentPointActivity.this, "אירעה בעיה בהעלאה! נסה שוב", Toast.LENGTH_SHORT).show();
                            if(progress.isShowing())
                                progress.dismiss();
                        }
                    });
                    Toast.makeText(AddRentPointActivity.this, "Success upload ", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddRentPointActivity.this, "Failed upload " + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    if(progress.isShowing())
                        progress.dismiss();
                }
            });



        } catch (Exception e) {
            Log.i(Classes.TAG,e.getMessage());
            if(progress.isShowing())
                progress.dismiss();
            e.printStackTrace();
        }
    }
    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public void addPoint(View view) {
        if(manualRb.isChecked()) {
            new MyAsyncTask(AddRentPointActivity.this).execute();
        }else {
            if(lat==0 || lon == 0){
                Toast.makeText(this, "לא הצלחנו לזהות מיקום, אנא רשום ידנית את הכתובת", Toast.LENGTH_SHORT).show();
            }else {
                setCurrentLocation();

            }
        }
     //   uploadImage();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(Classes.TAG,"onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Classes.TAG,"onResume");

    }

    AlertDialog.Builder addressesDialog;
    private class MyAsyncTask extends AsyncTask<Object, Object, List<Address>> {
        private ProgressDialog dialog;
        private Context context;

        public MyAsyncTask(Context context) {
            this.context = context;
        }

        // method that return list of the addresses
        private List<Address> searchAddresses() {
            geocoder = new Geocoder(AddRentPointActivity.this);
            try {
                Log.i(Classes.TAG,cityEt.getText().toString() + " "+ addressEt.getText().toString());
                return geocoder.getFromLocationName(cityEt.getText().toString() + " "+ addressEt.getText().toString(), MAX_RESULT);
            } catch (IOException e) {
            }
            return null;
        }

        // on the background , go and get the addresses , and then cancle the loading dialog
        @Override
        protected List<Address> doInBackground(Object... params) {
            return searchAddresses();
        }

        protected void onPreExecute() {
            // loading dialog is waiting until the search is finished!
            this.dialog = new ProgressDialog(context);
            this.dialog.setMessage("מחפש מיקום...");
            this.dialog.setCancelable(true);
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // cancel AsyncTask
                    cancel(false);
                }
            });

            this.dialog.show();

        }

        protected void onPostExecute(List<Address> result) {
            lstAdresses = result;
            if (lstAdresses == null) {
                Toast.makeText(context, "לא נמצאה כתובת", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;

            }
            if (lstAdresses.size() > 1) {
                // if the list size is more than 1 result , init a array from type charSequence in order to put them in the dialog
                // and then if the user choosed any item , set the text into the textView of the target !

                addressesDialog = new AlertDialog.Builder(AddRentPointActivity.this);
                final CharSequence items[] = new CharSequence[lstAdresses.size()];
                /*for (int i = 0; i < lstAdresses.size(); i++) {
                    Address location = lstAdresses.get(i);
                    for(int k=0;k<location.getMaxAddressLineIndex();k++) {
                        if(location.getAddressLine(k)!=null)
                        items[i] = location.getAddressLine(k) + " ";
                    }
                }*/
                for(int i = 0; i< lstAdresses.size(); i++)
                {
                    Address location= lstAdresses.get(i);

                    items[i]=location.getAddressLine(0)+" "+location.getAddressLine(1);
                }
                dialog.dismiss();



                addressesDialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface d, int n) {
                        Address location = lstAdresses.get(n);
                        // if their is one Result so set it to the textview

                        lon = lstAdresses.get(n).getLongitude();
                        lat = lstAdresses.get(n).getLatitude();
                        d.dismiss();
                        uploadImage();

                    }

                });
                addressesDialog.setNegativeButton("בחר", null);
                addressesDialog.setTitle("בחר אחת מהכתובות");
                addressesDialog.show();

            } else if (lstAdresses.size() == 0) {

                Log.i(Classes.TAG, "כתובת אינה חוקית!");
                Toast.makeText(context, "לא נמצאה כתובת", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } else {

                Address location = lstAdresses.get(0);
                // if their is one Result so set it to the textview
                Log.i(Classes.TAG, "" + location.getAddressLine(0) + " " + location.getAddressLine(1));

                Log.i(Classes.TAG, "Longtude:" + lstAdresses.get(0).getLongitude() + ", Latitude:" + lstAdresses.get(0).getLatitude());
                lon = lstAdresses.get(0).getLongitude();
                lat = lstAdresses.get(0).getLatitude();
                dialog.dismiss();
                uploadImage();


            }
            if(dialog.isShowing())
                dialog.dismiss();
        }
    }

        private void setPointToList(String imagePath) {
            Log.i(Classes.TAG,"in set point to list" );

            String phone = phoneEt.getText().toString();
        String description = descriptionEt.getText().toString();
        int area = Integer.parseInt(areaEt.getText().toString());
        String city = cityEt.getText().toString();
        String address = addressEt.getText().toString();
        String contact = contactEt.getText().toString();
        String type = currentType;
        int establishYear = Integer.parseInt(establishYearEt.getText().toString());

        Log.i(Classes.TAG,phone + ", "+description + ", "+area + ", "+city + ", "+address + ", "+type );
        String key =  pointsTable.child(mAuth.getUid()).push().getKey();
        //String type, Double lat, Double lon, String city, String address, String phone, String ownerName, String description, int area, int establishYear, String photoPath
        pointsTable.child(mAuth.getUid()).child(key).setValue(new RentPoint(key,type,lat,lon,city,address,phone,contact,description,area,establishYear, imagePath,null));
        progress.dismiss();
        onBackPressed();
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("הפעל GPS")
                .setCancelable(false)
                .setPositiveButton("הגדרות",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                            }
                        });
        alertDialogBuilder.setNegativeButton("בטל",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
