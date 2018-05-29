package tech.nadlan.com.nadlanproject;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.github.florent37.androidslidr.Slidr;

import org.angmarch.views.NiceSpinner;

public class AddRentPointActivity extends AppCompatActivity {
    public NiceSpinner typeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rent_point);
        typeSpinner = (NiceSpinner) findViewById(R.id.typeSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddRentPointActivity.this,
                android.R.layout.simple_spinner_item, new String[]{"דירה", "מגרש" , "עסק"});
        typeSpinner.setAdapter(adapter);
        typeSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
         }
}
