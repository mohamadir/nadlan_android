package tech.nadlan.com.nadlanproject.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kosalgeek.android.photoutil.GalleryPhoto;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.nadlan.com.nadlanproject.Adapters.FilesAdapter;
import tech.nadlan.com.nadlanproject.Adapters.PointsListAdapter;
import tech.nadlan.com.nadlanproject.Classes;
import tech.nadlan.com.nadlanproject.Models.FileMedia;
import tech.nadlan.com.nadlanproject.Models.RentPoint;
import tech.nadlan.com.nadlanproject.R;

public class MyFilesActivity extends AppCompatActivity implements  ValueEventListener {
    ListView fileListView;
    public List<FileMedia> fileList = new ArrayList<FileMedia>();
    FilesAdapter adapter;
    LinearLayout uploadLinear;
    public static GalleryPhoto galleryPhoto;
    public static ArrayList<String> photoPaths = new ArrayList<>();
    public String fileName = "";
    private DatabaseReference filesTable = FirebaseDatabase.getInstance().getReference("files");
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_files);
        fileListView = (ListView)findViewById(R.id.files_listView);
        uploadLinear = (LinearLayout) findViewById(R.id.uploadLinear);
        galleryPhoto = new GalleryPhoto(MyFilesActivity.this);
        uploadLinear.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MyFilesActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    uploadFileDialog();
                }
            }
        });
        mAuth = FirebaseAuth.getInstance();

    }
    DatabaseReference points;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        } else {
            uploadFileDialog();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        points = FirebaseDatabase.getInstance().getReference("files").child(mAuth.getUid()).getRef();
        points.addValueEventListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        points.removeEventListener(this);

    }

    private void uploadFileDialog() {
        View closeDialog = LayoutInflater.from(MyFilesActivity.this).inflate(R.layout.upload_dialog_layout, null);
        final Button uploadBt = (Button) closeDialog.findViewById(R.id.closeBt);
        final Button cancelBt = (Button) closeDialog.findViewById(R.id.cancelBt);
        final EditText fileNameEt = (EditText)closeDialog.findViewById(R.id.fileNameEt);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(closeDialog);

        final android.support.v7.app.AlertDialog dialog4 = builder.create();
        dialog4.show();
        uploadBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileNameEt.getText().toString().isEmpty()){
                    Toast.makeText(MyFilesActivity.this, "נא הכנס שם לקובץ", Toast.LENGTH_SHORT).show();
                }else {
                    fileName = fileNameEt.getText().toString();
                    pickFile();
                }
                dialog4.dismiss();

            }
        });
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog4.dismiss();
            }
        });
    }
    public   ProgressDialog progress;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null) {
            Log.i("FILEUPLOAD-","Data is null");
            return;
        }
        progress = new ProgressDialog(MyFilesActivity.this);;
        progress.setMessage("מעלה קובץ..");
        progress.show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = new File(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS).get(0));
                    String content_type = getMimeType(f.getPath().replaceAll(" ","%20"));
                    String file_path = f.getAbsolutePath().toString();
                    OkHttpClient client = new OkHttpClient();
                    if(content_type==null)
                        content_type="application/pdf";

                    RequestBody file_body;
                    file_body = RequestBody.create(MediaType.parse(content_type), f);
                    RequestBody request_body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type", content_type)
                            .addFormDataPart("file", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                            .build();
                    Request request = new Request.Builder()
                            .url("https://api.snapgroup.co.il/api/upload/74")
                            .post(request_body)
                            .build();

                    try {
                        Response response = client.newCall(request).execute();
                         String jsonData = response.body().string();
                        JSONObject Jobject = new JSONObject(jsonData);
                        String filePath=Jobject.getString("path");
                        progress.dismiss();
                        uploadToFireBase(filePath);

                        if (!response.isSuccessful()) {
                            throw new IOException("Error : " + response);
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        if(progress.isShowing())
                        progress.dismiss();

                        Log.i("FILEUPLOAD-","1file upload failed " + e.getMessage().toString());

                    }

                } catch (Exception e) {
                    if(progress.isShowing())
                        progress.dismiss();
                    Log.i("FILEUPLOAD-","2file upload failed " + e.getMessage().toString());
                }

            }
        });

        t.start();
    }

    private void uploadToFireBase(String path) {
        Log.i("FILEUPLOAD","path: https://api.snapgroup.co.il"+path + ",   name: "+fileName);
        // ADD TO FIRE BASE
        String key =  filesTable.child(mAuth.getUid()).push().getKey();
        //String type, Double lat, Double lon, String city, String address, String phone, String ownerName, String description, int area, int establishYear, String photoPath
        filesTable.child(mAuth.getUid()).child(key).setValue(new FileMedia(fileName,Classes.UPLOAD_SERVER+path));

    }

    private String getMimeType(String path) {

        String extension = getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
    public static String getFileExtensionFromUrl(String url) {
        if (url != null && url.length() > 0) {
            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf('/');
            String filename =
                    0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            // if the filename contains special characters, we don't
            // consider it valid for our matching purposes:
            if (filename.length() > 0 &&
                    Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)]+", filename)) { // <--
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }

        return "";
    }
    private void pickFile() {
        FilePickerBuilder.getInstance().setMaxCount(1)
                .setSelectedFiles(MyFilesActivity.photoPaths)
                .setActivityTheme(R.style.AppTheme)
                .pickFile(MyFilesActivity.this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        fileList.clear();
        for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
            FileMedia file = userSnapShot.getValue(FileMedia.class);
            fileList.add(file);
        }
        adapter = new FilesAdapter(MyFilesActivity.this,fileList);
        fileListView.setAdapter(adapter);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
