package smap.gr15.appproject.tendr.utils;

import android.util.Log;

public class helpers {

    // Used to decide who to prefer in the start of the app, so if you're a woman, you would as standard prefer man
    public static String setGenderOpposite(String gender)
    {
        switch (gender){
            case "Female":
                return "Male";
            case "Male":
                return "Female";
        }

        //This should never get reached
        return "Female";
    }
}
