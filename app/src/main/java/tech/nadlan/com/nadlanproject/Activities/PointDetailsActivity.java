package tech.nadlan.com.nadlanproject.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import tech.nadlan.com.nadlanproject.Classes;
import tech.nadlan.com.nadlanproject.R;

public class PointDetailsActivity extends AppCompatActivity {
    ImageView imageview;
    TextView titleTv,subTitleTv,phoneTv,contactTv,descriptionTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_details);
        imageview = (ImageView)findViewById(R.id.imageview);
        titleTv = (TextView)findViewById(R.id.title_Tv);
        subTitleTv = (TextView)findViewById(R.id.subtitle_Tv);
        phoneTv = (TextView)findViewById(R.id.phoneTv);
        descriptionTv = (TextView)findViewById(R.id.descriptionTv);
        contactTv = (TextView)findViewById(R.id.contactTv);
        phoneTv.setText(Classes.currentRp.getPhone());
        descriptionTv.setText(Classes.currentRp.getDescription());
        contactTv.setText(Classes.currentRp.getOwnerName());
        titleTv.setText(getIntent().getStringExtra("title"));
        subTitleTv.setText(getIntent().getStringExtra("subtitle"));
        Log.i(Classes.TAG,"Current site "+Classes.currentRp.getPhotoPath());
        Picasso.with(this).load(Classes.currentRp.getPhotoPath()).into(imageview);

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
}
