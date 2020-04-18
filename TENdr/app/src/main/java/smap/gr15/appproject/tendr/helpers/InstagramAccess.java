package smap.gr15.appproject.tendr.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


// FOR FURTHER DETAILS SEE Instagram Basic Display API - https://developers.facebook.com/docs/instagram-basic-display-api
public class InstagramAccess {

    private static final String INSTAGRAM_USER_ACCESS_TOKENS_URL = "api.instagram.com";
    private static final String INSTAGRAM_PROFILES_AND_MEDIA = "graph.instagram.com";


    public void setAccessToken(Context context, String key, String value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(key,value).apply();
    };

    public String getAccessToken(Context context, String key)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, null);
    }
};
