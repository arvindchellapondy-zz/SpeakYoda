package zerobase.us.speakyoda;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

/**
 * Created by arvindchellapondy on 12/2/15.
 */
public class SplashActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    }

    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent in = new Intent(SplashActivity.this,YodaActivity.class);
                startActivity(in);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }


}
