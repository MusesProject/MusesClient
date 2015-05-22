package eu.musesproject.client.connectionmanager;

import android.content.Context;
import android.content.SharedPreferences;
import eu.musesproject.client.ui.MainActivity;
import org.apache.http.cookie.Cookie;

import java.util.Date;

/**
 * Created by yasir on 22/05/2015.
 */
public class SessionCookie implements Cookie {
    SharedPreferences prefs;

    @Override
    public String getName() {
        prefs = ConnectionManager.context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        if (prefs.contains("name")){
            return prefs.getString("name","");
        }
        return null;
    }

    @Override
    public String getValue() {

        prefs = ConnectionManager.context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        if (prefs.contains("value")){
            return prefs.getString("value", "");
        }

        return null;
    }

    @Override
    public String getComment() {

        prefs = ConnectionManager.context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        if (prefs.contains("comment")){
            return prefs.getString("comment", "");
        }
        return null;
    }

    @Override
    public String getCommentURL() {
        return null;
    }

    @Override
    public Date getExpiryDate() {
        prefs = ConnectionManager.context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        if (prefs.contains("expiry")){
            return new Date(prefs.getLong("expiry",System.currentTimeMillis()));
        }
        return new Date(System.currentTimeMillis());
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public String getDomain() {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public int[] getPorts() {
        return new int[0];
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public boolean isExpired(Date date) {
        Date now = new Date();
        if (now.compareTo(date) <= 0){
            return false;
        }else {
            return true;
        }

    }



}
