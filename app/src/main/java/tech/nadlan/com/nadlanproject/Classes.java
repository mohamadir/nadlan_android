package tech.nadlan.com.nadlanproject;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by snapmac on 5/25/18.
 */

public class Classes {
    public static Boolean isLogged = false;
    public static User currentUser = new User();
    public static final String CONTENT_AUTHORITY = "br.com.mauker.materialsearchview.searchhistorydatabase";


    public static void saveToShp(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("user_pref", context.MODE_PRIVATE).edit();
        editor.putBoolean("isLogged", true);
        editor.putString("email", Classes.currentUser.email);
        editor.putString("phone", Classes.currentUser.phone);
        editor.putString("username", Classes.currentUser.username);
        editor.putString("id", Classes.currentUser.id);
        editor.commit();
    }
}
