package tech.nadlan.com.nadlanproject.Models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by snapmac on 5/25/18.
 */

@IgnoreExtraProperties
public class User {


    public String username;
    public String phone;

    public User( String username, String phone) {
        this.username = username;
        this.phone = phone;
    }
    public User(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}