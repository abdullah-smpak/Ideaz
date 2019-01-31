package com.example.mimmadjaved.ideazshuttle;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class MJobSch extends JobService {
    private MJobExecuter mJobExecuter;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
      //  int second = calendar.get(Calendar.SECOND);
        String timein,timeout;
        timein = hour+":"+minute;
        timeout = hour+":"+minute;
       // Toast.makeText(this, hour+":"+minute, Toast.LENGTH_SHORT).show();
        if(timein.equals("8:50"))
        {
          //  Toast.makeText(MJobSch.this, "App is Starting", Toast.LENGTH_SHORT).show();
            Intent launchIntent =getPackageManager().getLaunchIntentForPackage("com.example.mimmadjaved.ideazshuttle");


            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }
            jobFinished(jobParameters,false);

        }


        if(timeout.equals("5:50"))
        {
           // Toast.makeText(MJobSch.this, "App is Starting", Toast.LENGTH_SHORT).show();
            Intent launchIntent =getPackageManager().getLaunchIntentForPackage("com.example.mimmadjaved.ideazshuttle");

            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }
            jobFinished(jobParameters,false);

        }




       // mJobExecuter.execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
     //  mJobExecuter.cancel(true);
        return false;
    }
}
