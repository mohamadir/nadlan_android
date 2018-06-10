package tech.nadlan.com.nadlanproject.Activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.nadlan.com.nadlanproject.Classes;
import tech.nadlan.com.nadlanproject.Models.FileMedia;
import tech.nadlan.com.nadlanproject.R;

public class PointDetailsActivity extends AppCompatActivity {
    ImageView imageview;
    TextView titleTv,subTitleTv,phoneTv,contactTv,descriptionTv;
    LinearLayout addFileLn;
    CardView fileCv;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_details);
        imageview = (ImageView)findViewById(R.id.imageview);
        titleTv = (TextView)findViewById(R.id.title_Tv);
        subTitleTv = (TextView)findViewById(R.id.subtitle_Tv);
        phoneTv = (TextView)findViewById(R.id.phoneTv);
        descriptionTv = (TextView)findViewById(R.id.descriptionTv);
        addFileLn = (LinearLayout) findViewById(R.id.addFileLn);
        contactTv = (TextView)findViewById(R.id.contactTv);
        phoneTv.setText(Classes.currentRp.getPhone());
        descriptionTv.setText(Classes.currentRp.getDescription());
        contactTv.setText(Classes.currentRp.getOwnerName());
        titleTv.setText(getIntent().getStringExtra("title"));
        subTitleTv.setText(getIntent().getStringExtra("subtitle"));
        fileCv = (CardView)findViewById(R.id.fileCv);
        Log.i(Classes.TAG,"Current site "+Classes.currentRp.getPhotoPath());
        Picasso.with(this).load(Classes.currentRp.getPhotoPath()).into(imageview);
        if(Classes.currentRp.getPoint_file() != null){
            addFileLn.setVisibility(View.GONE);
            fileCv.setVisibility(View.VISIBLE);
            setFile(Classes.currentRp.getPoint_file().getFileName().toString(),Classes.currentRp.getPoint_file().getPath());
        }else {
            addFileLn.setVisibility(View.VISIBLE);
            fileCv.setVisibility(View.GONE);
        }
        mAuth = FirebaseAuth.getInstance();


    }

    private void setFile(String fileNameString, final String filePath) {
        TextView fileName = (TextView)findViewById(R.id.fileName_tv);
        fileName.setText(fileNameString);
        findViewById(R.id.downloadTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PointDetailsActivity.this, "הקובץ בהורדה..", Toast.LENGTH_SHORT).show();
                DownloadManager downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri =   Uri.parse(filePath);
                DownloadManager.Request request=new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                Long reference=downloadManager.enqueue(request);
            }
        });

    }

    public void shareClick(View view) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"משתף אותך דירה שפרסמתי באפליקציית Nadlan");
        String textToShare = titleTv.getText().toString() + " \n" + subTitleTv.getText().toString() + "\n\n\n" + "הזדמנות להשכרה או למכירה" + "\n"
            + " לפרטים נוספים ניתן ליצור קשר דרך מספר הטלפון: " +  "\n" + Classes.currentUser.getPhone() +"\n\n" + "שותף מאפליקציית Nadlan" ;
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(sharingIntent, "שתף את הפרויט שלך"));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void addFileClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            uploadFileDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
       &&  ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        } else {
            uploadFileDialog();
        }
    }

    public   ProgressDialog progress;
    public Intent FileData = null;
    public String fileName = "";


    private void uploadFileDialog() {
        View closeDialog = LayoutInflater.from(PointDetailsActivity.this).inflate(R.layout.upload_dialog_layout, null);
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
                    Toast.makeText(PointDetailsActivity.this, "נא הכנס שם לקובץ", Toast.LENGTH_SHORT).show();
                }else {
                    fileName = fileNameEt.getText().toString();
                    pickFile();
                    dialog4.dismiss();
                }

            }
        });
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog4.dismiss();
            }
        });
    }
    private void pickFile() {
        FilePickerBuilder.getInstance().setMaxCount(1)
                .setSelectedFiles(MyFilesActivity.photoPaths)
                .setActivityTheme(R.style.AppTheme)
                .pickFile(PointDetailsActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null) {
            FileData = data;
            uploadFile();
        }
    }

    public void uploadFile(){
        progress = new ProgressDialog(PointDetailsActivity.this);;
        progress.setMessage("מעלה קובץ..");
        progress.show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = new File(FileData.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS).get(0));
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
                        final String filePath=Jobject.getString("path");
                        PointDetailsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uploadToFireBase(filePath);

                            }
                        });
                        Log.i("FILEUPLOAD-",filePath);
                        progress.dismiss();
                    //    uploadToFireBase(filePath);

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

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void uploadToFireBase(String path) {
        addFileLn.setVisibility(View.GONE);
        fileCv.setVisibility(View.VISIBLE);
        setFile(fileName,Classes.UPLOAD_SERVER+path);
        final DatabaseReference points = FirebaseDatabase.getInstance().getReference("rent_points").child(mAuth.getUid()).getRef();
        //String type, Double lat, Double lon, String city, String address, String phone, String ownerName, String description, int area, int establishYear, String photoPath
        points.child(Classes.currentRp.getId()).child("point_file").setValue(new FileMedia(fileName,Classes.UPLOAD_SERVER+path));

        Toast.makeText(this, "הקובץ הועלה בהצלחה", Toast.LENGTH_SHORT).show();
    }
    private void removeFile() {
        addFileLn.setVisibility(View.VISIBLE);
        fileCv.setVisibility(View.GONE);
        final DatabaseReference points = FirebaseDatabase.getInstance().getReference("rent_points").child(mAuth.getUid()).getRef();
        //String type, Double lat, Double lon, String city, String address, String phone, String ownerName, String description, int area, int establishYear, String photoPath
        points.child(Classes.currentRp.getId()).child("point_file").setValue(null);
        Toast.makeText(this, "הקובץ הועלה בהצלחה", Toast.LENGTH_SHORT).show();
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

    public void removeFilefromFirebase(View view) {
        removeFile();
    }
}
