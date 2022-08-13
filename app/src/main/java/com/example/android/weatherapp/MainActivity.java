package com.example.android.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout Rlayout;
    private ProgressBar progressBar;
    private TextView cityname,temp,condition;
    private RecyclerView recyclerView;
    private EditText editText;
    private ImageView search,icon,back;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private  int PERMISSION_CODE=1;
    private String City_Name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        Rlayout=findViewById(R.id.Relative_1);
        progressBar=findViewById(R.id.progress);
        cityname=findViewById(R.id.City_Name);
        temp=findViewById(R.id.temp_1);
        condition=findViewById(R.id.weather_condition_text);
        recyclerView=findViewById(R.id.weather_recycler);
        search=findViewById(R.id.search_button);
        editText=findViewById(R.id.Edit_text_1);
        icon=findViewById(R.id.weather_condition_icon);
        back=findViewById(R.id.black);
        weatherRVModalArrayList=new ArrayList<>();
        weatherRVAdapter=new WeatherRVAdapter(this,weatherRVModalArrayList);
        recyclerView.setAdapter(weatherRVAdapter);
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location==null)
            System.out.println("this is null");
       // System.out.println("this is null");
        //assert location != null;
        if (location != null) {
            City_Name=getCityname(location.getLongitude(),location.getLatitude());
            //Toast.makeText(this, City_Name, Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "location is null", Toast.LENGTH_SHORT).show();
        getweatherinfo(City_Name);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city=editText.getText().toString();
                //Toast.makeText(MainActivity.this, city, Toast.LENGTH_SHORT).show();
                if(city.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"Please enter the city name",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    cityname.setText(city);
                    getweatherinfo(city);
                    //Toast.makeText(MainActivity.this,city,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE)
        {
            Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();;
            finish();
        }
    }

    private String getCityname(double longitude, double latitude)
    {
        String Cityname="Not Found";
        Geocoder gcd=new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses=gcd.getFromLocation(latitude,longitude,10);


            for(Address x:addresses)
            {
                if(x!=null)
                {
                    String city=x.getLocality();
                    if(city!=null&&!city.equals(""))
                    {
                        Cityname=city;
                    }
                    else
                    {
                        Log.d("Tag","City Not found");
                        //Toast.makeText(this,"User city not found",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return Cityname;
    }
    private  void getweatherinfo(String Cityname){
        String url="https://api.weatherapi.com/v1/forecast.json?key=14571ebc58a9428697b150938211109&q="+Cityname+"&days=1&aqi=yes&alerts=yes";
        cityname.setText(Cityname);
        //Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);

                Rlayout.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temp.setText(temperature + "°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condn = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionIcon)).into(icon);
                    condition.setText(condn);
                    if(isDay==1)
                    {
                        back.setImageResource(R.drawable.day);
                    }
                    else
                        back.setImageResource(R.drawable.night);

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecast = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourarray = forecast.getJSONArray("hour");

                    for (int i = 0; i < hourarray.length(); i++) {
                        JSONObject hourObj = hourarray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please Enter the valid city name",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}