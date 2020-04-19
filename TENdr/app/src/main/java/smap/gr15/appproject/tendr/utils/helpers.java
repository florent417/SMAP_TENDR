package smap.gr15.appproject.tendr.utils;

import android.util.Log;

public class helpers {

    public static String setGenderOpposite(String gender)
    {
        Log.d("GENDER", gender);

        switch (gender){
            case "Female":
                return "Male";
            case "Male":
                return "Female";
        }

        return "Female";
    }
}
