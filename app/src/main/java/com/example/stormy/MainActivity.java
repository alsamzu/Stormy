package com.example.stormy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;

    private TextView mTimeLabel;
    private TextView mHumidityValue;
    private TextView mTemperatureLabel;
    private TextView mPrecipValue;
    private TextView mSummaryLabel;
    private ImageView mIconImageView;
    private ImageView mRefreshImageView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        double latitude = 37.8267;
        double longitude = -122.4233;

        mTimeLabel = (TextView) findViewById(R.id.timeLabel);
        mHumidityValue = (TextView) findViewById(R.id.humidityValue);
        mTemperatureLabel = (TextView) findViewById(R.id.temperatureLabel);
        mPrecipValue = (TextView) findViewById(R.id.precipValue);
        mSummaryLabel = (TextView) findViewById(R.id.summaryLabel);
        mIconImageView = (ImageView) findViewById(R.id.iconImageView);
        mRefreshImageView = (ImageView) findViewById(R.id.refreshImageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.VISIBLE);



        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForeCast(latitude,longitude);
            }
        });

        getForeCast(latitude,longitude);
        Log.d(TAG,"Main UI code is running!");
    }

    private void getForeCast(double latitude,double longitude) {
        String apiKey = "b00e0bcd6f9e290d446d392c8adc01cf";

        String forecastUrl = "https://api.darksky.net/forecast/"+apiKey+"/"
                +latitude+","+longitude;

        if (isNetworkAvailable()){
            toggleRefresh();
            OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(forecastUrl).build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                      runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              toggleRefresh();
                          }
                      });
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG,jsonData);
                    if (response.isSuccessful()){
                        mCurrentWeather=getCurrentDetails(jsonData);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });

                    }
                    else{
                        alertUserAboutError();
                    }
                }
                catch (IOException e) {
                    Log.e(TAG,"Exception error caught: ",e);
                }
                catch (Exception e) {
                    Log.e(TAG,"Exception error caught: ",e);
                }
            }
        });
        }
        else {
            Toast.makeText(this,"Network not Available", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }

    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature()+"");
        mTimeLabel.setText("At "+mCurrentWeather.getFormatedTime()+" it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity()+"");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance()+"%");

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);



    }

    private CurrentWeather getCurrentDetails(String jsonData) throws Exception {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject currently = forecast.getJSONObject("currently");
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        Log.d(TAG,currentWeather.getFormatedTime());
        return currentWeather;
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
            return isAvailable;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"error_dialog");

    }
}
