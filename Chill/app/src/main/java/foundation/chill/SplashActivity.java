package foundation.chill;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class SplashActivity extends AppCompatActivity {

    private static boolean splashLoaded = false;
    RelativeLayout splashLayout;

    Runnable run;
    Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!splashLoaded) {
            setContentView(R.layout.activity_splash);
            splashLayout = (RelativeLayout)findViewById(R.id.activity_splash);

            int secondsDelayed = 1;

            run = new Runnable(){
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            };
            handler = new Handler();
            handler.postDelayed(run, secondsDelayed * 5000);
            
            splashLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    handler.removeCallbacks(run);
                }
            });


            splashLoaded = true;
        }
        else {
            Intent goToFacebookActivity = new Intent(SplashActivity.this, MainActivity.class);
            goToFacebookActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(goToFacebookActivity);
            finish();
        }
    }
}
