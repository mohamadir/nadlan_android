package tech.nadlan.com.nadlanproject;

import android.*;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.florent37.androidslidr.Slidr;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kosalgeek.android.photoutil.GalleryPhoto;

import org.angmarch.views.NiceSpinner;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddRentPointActivity extends AppCompatActivity {
    public NiceSpinner typeSpinner;
    public static GalleryPhoto galleryPhoto;
    public Intent ImageData = null;
    public String currentType = "דירה";
    public EditText phoneEt,cityEt,addressEt,contactEt,establishYearEt,descriptionEt,areaEt;
    public String[] types =  new String[]{"דירה", "מגרש" , "עסק"};
    public ImageView pointImage;
    public FirebaseAuth mAuth;
    private DatabaseReference pointsTable = FirebaseDatabase.getInstance().getReference("rent_points");
    public String imagePath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rent_point);
        typeSpinner = (NiceSpinner) findViewById(R.id.typeSpinner);
        galleryPhoto = new GalleryPhoto(AddRentPointActivity.this);
        initializeViews();
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(galleryPhoto.openGalleryIntent(), Classes.GALLERY_REQUEST);

        }else
        {
            Log.i("perAsk","in else");
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
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
        if (requestCode == Classes.GALLERY_REQUEST && resultCode == RESULT_OK
                && null != data) {
            if (data == null) {
                return;
            }else {
                Uri uri = data.getData();
                galleryPhoto.setPhotoUri(uri);
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(data.getData());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                pointImage.setImageBitmap(bitmap);
                ImageData = data;}
        }
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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {


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
                            .url("https://api.snapgroup.co.il/api/upload_single_image/Member/74/gallery")
                            .post(request_body)
                            .build();

                    try {
                        Response response = client.newCall(request).execute();
                        String jsonData = response.body().string();
                        JSONObject Jobject = new JSONObject(jsonData);

                        JSONObject imageObj=Jobject.getJSONObject("image");
                        imagePath= imageObj.getString("path");
                        Log.i(Classes.TAG, "SUCCESS UPLOAD "+imagePath);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setPointToList("https://api.snapgroup.co.il"+imagePath);
                            }
                        });
                        if (!response.isSuccessful()) {
                            throw new IOException("Error : " + response);
                        }


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
                        AddRentPointActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(AddRentPointActivity.this, "Cannot upload\n Please trye another file!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }

        });


        t.start();

    } catch (Exception e) {
        Log.i(Classes.TAG,e.getMessage());
        e.printStackTrace();
    }
    }
    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public void addPoint(View view) {
        uploadImage();

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

    private void setPointToList(String imagePath) {
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
        pointsTable.child(mAuth.getUid()).child(key).setValue(new RentPoint(type,31.749997,35.2166658,city,address,phone,contact,description,area,establishYear, imagePath));
        progress.dismiss();
        onBackPressed();
    }
}
