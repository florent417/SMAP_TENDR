package smap.gr15.appproject.tendr.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import smap.gr15.appproject.tendr.activities.MainActivity;
import smap.gr15.appproject.tendr.activities.ProfileActivity;
import smap.gr15.appproject.tendr.activities.SettingsActivity;

import static android.content.Context.ACTIVITY_SERVICE;

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

        return "Female";
    }

    public static void setupCustomActionBar(ImageButton settings, ImageButton main, ImageButton profile, Context context)
    {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.getClass() != SettingsActivity.class)
                {
                    checkIfActivityisRunning(context);
                    Intent intent = new Intent(context, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                }
            }
        });

        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.getClass() != MainActivity.class)
                {
                    checkIfActivityisRunning(context);
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                }
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.getClass() != ProfileActivity.class)
                {
                    checkIfActivityisRunning(context);
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                }
            }
        });
    }

    // Partly inspired from https://stackoverflow.com/questions/5975811/how-to-check-if-an-activity-is-the-last-one-in-the-activity-stack-for-an-applica
    private static void checkIfActivityisRunning(Context context)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService( ACTIVITY_SERVICE );

        try{
            List<ActivityManager.RunningTaskInfo> taskInfoList = manager.getRunningTasks(15);

            Integer i = 0;
            for (ActivityManager.RunningTaskInfo task : taskInfoList)
            {
                i++;
                Log.d("INTEGER", i.toString());
                Log.d("taskGetClass", task.getClass().toString());
                Log.d("taskGetClass", task.baseActivity.getShortClassName());

                Log.d("contextGetClass", context.getClass().toString());
                if(task.getClass().equals(context.getClass()))
                {
                    Log.d("ALREADY RUNNING", "TRUE");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
