package tech.nadlan.com.nadlanproject;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;

import tech.nadlan.com.nadlanproject.Models.RentPoint;
import tech.nadlan.com.nadlanproject.Models.User;

/**
 * Created by snapmac on 5/25/18.
 */

public class Classes {
    public static final String TAG = "MOHAMED-LOG";
    public static final int GALLERY_REQUEST = 22131;
    public static String currentEmail = "";
    public static String UPLOAD_SERVER = "https://api.snapgroup.co.il";
    public static final String TYPE_APARTMENT = "דירה";
    public static final String TYPE_LAND = "מגרש";
    public static final String TYPE_BUSINESS = "עסק";

    public GoogleMap mMap;

    public static Boolean isLogged = false;
    public static User currentUser = new User();
    public static RentPoint currentRp = new RentPoint();
    FirebaseAuth currentAuth = FirebaseAuth.getInstance();
    public static final String CONTENT_AUTHORITY = "br.com.mauker.materialsearchview.searchhistorydatabase";


    public static void saveToShp(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("user_pref", context.MODE_PRIVATE).edit();
        editor.putBoolean("isLogged", true);
        editor.putString("phone", Classes.currentUser.phone);
        editor.putString("username", Classes.currentUser.username);
        editor.commit();
    }
}
