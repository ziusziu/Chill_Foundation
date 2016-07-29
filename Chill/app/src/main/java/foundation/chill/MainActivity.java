package foundation.chill;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    Button color1Button;
    Button color2Button;
    Button color3Button;
    Button color4Button;
    TextView snowFallTextView;
    TextView temperatureTextView;
    TextView elevationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initColorButtons();


    }

    private void initializeViews(){
        color1Button = (Button)findViewById(R.id.color1_button);
        color2Button = (Button)findViewById(R.id.color2_button);
        color3Button = (Button)findViewById(R.id.color3_button);
        color4Button = (Button)findViewById(R.id.color4_button);
        snowFallTextView = (TextView)findViewById(R.id.snowfall_textView);
        temperatureTextView = (TextView)findViewById(R.id.temperature_textView);
        elevationTextView = (TextView)findViewById(R.id.elevation_textView);
    }

    private void initColorButtons(){
        setButtonOnClickListener(color1Button);
        setButtonOnClickListener(color2Button);
        setButtonOnClickListener(color3Button);
        setButtonOnClickListener(color4Button);
    }

    private void setButtonOnClickListener(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.color1_button:
                        Log.d(TAG, "Button 1 Clicked");
                        break;
                    case R.id.color2_button:
                        Log.d(TAG, "Button 2 Clicked");
                        break;
                    case R.id.color3_button:
                        Log.d(TAG, "Button 3 Clicked");
                        break;
                    case R.id.color4_button:
                        Log.d(TAG, "Button 4 Clicked");
                        break;
                    default:
                        Log.d(TAG, "NOTHING CLICKED");
                        break;
                }
            }
        });




    }


}
