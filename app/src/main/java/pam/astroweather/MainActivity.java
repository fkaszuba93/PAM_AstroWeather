package pam.astroweather;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.content.Intent;
import java.util.*;
import static java.util.Calendar.*;
import com.astrocalculator.*;

public class MainActivity extends AppCompatActivity {

    public static final String LATITUDE = "pam.astroweather.latitude";
    public static final String LONGITUDE = "pam.astroweather.longitude";
    public static final String FREQ = "pam.astroweather.freq";
    public static final int REQUEST_CODE_SETTINGS = 1;

    private TextView timeText, locationText;
    private SunFragment sunFragment;
    private MoonFragment moonFragment;
    private ViewPager fragmentPager;
    private LinearLayout fragmentLinearLayout;
    private Button settingsButton;
    private final Timer clock = new Timer(), infoUpdateTimer = new Timer();
    private AstroCalculator.Location location = new AstroCalculator.Location(51, 19);
    private int freq = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setComponents();
        if (savedInstanceState != null){
            location.setLatitude(savedInstanceState.getDouble(LATITUDE));
            location.setLongitude(savedInstanceState.getDouble(LONGITUDE));
            freq = savedInstanceState.getInt(FREQ);
        }
        updateLocation();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        setTimers();
    }

    @Override
    protected void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);
        state.putDouble(LATITUDE, location.getLatitude());
        state.putDouble(LONGITUDE, location.getLongitude());
        state.putInt(FREQ, freq);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK){
            location.setLatitude(data.getDoubleExtra(LATITUDE, location.getLatitude()));
            location.setLongitude(data.getDoubleExtra(LONGITUDE, location.getLongitude()));
            freq = data.getIntExtra(FREQ, 15);
            updateLocation();
            updateInfo();
            infoUpdateTimer.cancel();
            setInfoUpdateTimer(freq);
        }
    }

    private void setComponents(){
        settingsButton = (Button)findViewById(R.id.settings_button);
        timeText = (TextView)findViewById(R.id.time);
        locationText = (TextView)findViewById(R.id.location);
        fragmentPager = (ViewPager)findViewById(R.id.fragment_pager);
        fragmentLinearLayout = (LinearLayout)findViewById(R.id.fragment_linear_layout);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra(LATITUDE, location.getLatitude());
                intent.putExtra(LONGITUDE, location.getLongitude());
                intent.putExtra(FREQ, freq);
                startActivityForResult(intent, REQUEST_CODE_SETTINGS);
            }
        });
        if (fragmentPager != null){
            setPager();
            sunFragment = (SunFragment) ((FragmentPagerAdapter)fragmentPager.getAdapter()).getItem(0);
            moonFragment = (MoonFragment) ((FragmentPagerAdapter)fragmentPager.getAdapter()).getItem(1);
        }
        /*FragmentManager manager = getSupportFragmentManager();
        sunFragment = (SunFragment)manager.findFragmentById(R.id.sun_fragment);
        moonFragment = (MoonFragment)manager.findFragmentById(R.id.moon_fragment);
        if (sunFragment == null && moonFragment == null){
            sunFragment = new SunFragment();
            moonFragment = new MoonFragment();
            manager.beginTransaction()
                    .add(R.id.fragment_pager, sunFragment)
                    .add(R.id.fragment_pager, moonFragment)
                    .commit();
        }*/
    }

    private void setPager() {
        fragmentPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch(position) {
                    case 0:
                        return new SunFragment();
                    case 1:
                        return new MoonFragment();
                    default:
                        return null;
                }
            }
            @Override
            public int getCount() {
                return 2;
            }
        });
        fragmentPager.setCurrentItem(0);
    }

    private void setTimers(){
        clock.schedule(new TimerTask(){
            @Override
            public void run(){
                updateClock();
            }
        }, 0, 1000);
        setInfoUpdateTimer(15);
    }

    private void setInfoUpdateTimer(int minutes){
        infoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateInfo();
            }
        }, 0, minutes * 60000);
    }

    private void updateClock(){
        Calendar calendar = Calendar.getInstance();
        final Date t = calendar.getTime();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeText.setText(String.format("%tT", t));
            }
        });
    }

    private void updateInfo(){
        Calendar calendar = Calendar.getInstance();
        AstroDateTime time = new AstroDateTime(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH),
                calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), calendar.get(SECOND), 0, true);
        AstroCalculator calculator = new AstroCalculator(time, location);
        final AstroCalculator.SunInfo sunInfo = calculator.getSunInfo();
        final AstroCalculator.MoonInfo moonInfo = calculator.getMoonInfo();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sunFragment.update(sunInfo);
                moonFragment.update(moonInfo);
            }
        });
    }

    private void updateLocation(){
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String latitudeDirection = latitude > 0 ? "N" : "S";
        String longitudeDirection = longitude > 0 ? "E" : "W";
        locationText.setText(Math.abs(latitude) + " " + latitudeDirection + " " + Math.abs(longitude) + " " + longitudeDirection);
    }
}
