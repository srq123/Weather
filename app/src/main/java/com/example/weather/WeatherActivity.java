package com.example.weather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.weather.gson.Forecast;
import com.example.weather.gson.Weather;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import java.io.IOException;

import javax.security.auth.callback.Callback;

import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        weatherLayout = (ScrollView) findViewById(R.id.weatherLayout);
        titleCity = (TextView) findViewById(R.id.title_text);
        titleUpdateTime = (TextView) findViewById(R.id.titleUpdateTime);
        degreeText = (TextView) findViewById(R.id.degreeText);
        weatherInfoText = (TextView) findViewById(R.id.weatherInfoText);
        forecastLayout = (LinearLayout) findViewById(R.id.forecastLayout);
        aqiText = (TextView) findViewById(R.id.aqiText);
        pm25Text = (TextView) findViewById(R.id.pm25Text);
        comfortText = (TextView) findViewById(R.id.comfortText);
        carWashText = (TextView) findViewById(R.id.carWashText);
        sportText = (TextView) findViewById(R.id.sportText);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        final String weatherId;
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }
        else {
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }

    public void  requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        //String weatherUrl = "https://free-api.heweather.net/s6/weather/now?location=beijing&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Toast.makeText(WeatherActivity.this , "获取天气信息失败fai", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.e("response", responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败kkkk", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
//       String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "C";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        //titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.dateText);
            TextView infoText = (TextView) view.findViewById(R.id.infoText);
            TextView maxText = (TextView) view.findViewById(R.id.maxText);
            TextView minText = (TextView) view.findViewById(R.id.minText);
            dateText.setText(forecast.data);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carwash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
