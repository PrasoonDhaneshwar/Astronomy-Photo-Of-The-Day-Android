package com.prasoon.astronomypictureoftheday;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private String TAG = "MainActivity";

    private TextView mTextViewExplanation;
    private TextView mTextViewDate;
    private TextView mTextViewFavoritesJson;
    private TextView mTextViewTitle;
    private TextView mTextViewMetadataDate;

    private ImageView imageView;
    private Button mButtonParse;
    private Button mButtonSelectDateButton;
    private Button mButtonSelectSecondActivity;
    private Button buttonAddIntoFavorites;

    private RequestQueue mQueue;
    private String mCurrentDateString = null;
    private String mCurrentDateFormatInput = null;
    private String mUrlRequestDefault = null;
    private String mUrlRequestForJson = null;

    public static String mUrlRequestForJsonFavorites = null;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String URL_JSON = "urlJson";


    protected void setmFavoriteList(String jsonLink) {
        mFavoriteList.add(jsonLink);
    }

    protected static Set<String> mFavoriteList = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewExplanation = findViewById(R.id.textViewExplanation);
        mTextViewExplanation.setMovementMethod(new ScrollingMovementMethod());
        mTextViewDate = findViewById(R.id.textViewDatePicker);
        mButtonParse = findViewById(R.id.buttonParse);
        imageView = findViewById(R.id.imageViewResult);

        // Test out a new activity --start
        mButtonSelectSecondActivity = findViewById(R.id.selectSecondActivity);
        mButtonSelectSecondActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecyclerViewActivity();
            }
        });

        // --end
        mTextViewTitle = findViewById(R.id.textViewTitle);
        mTextViewMetadataDate = findViewById(R.id.textViewMetadataDate);


        mQueue = Volley.newRequestQueue(this);
        jsonParse();

        mButtonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonParse();
            }
        });


        mButtonSelectDateButton = findViewById(R.id.selectDateButton);
        mButtonSelectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });


        buttonAddIntoFavorites = findViewById(R.id.addIntoFavorites);
        buttonAddIntoFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataIntoFavoriteList();
            }
        });

        mTextViewFavoritesJson = findViewById(R.id.textViewFavoritesJson);

        loadDataIntoFavoriteList();
        mTextViewDate.setText("MMM, D, YYYY");
        // mTextViewDate.setText(mCurrentDateString);
    }

    // Shared preferences methods
    private void saveDataIntoFavoriteList() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        mUrlRequestForJsonFavorites = mUrlRequestForJson;
        mFavoriteList.add(mUrlRequestForJsonFavorites);

        setmFavoriteList(mUrlRequestForJsonFavorites);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(URL_JSON, mUrlRequestForJson);
        mTextViewFavoritesJson.setText(mUrlRequestForJsonFavorites);
        editor.apply();
        PrefConfig.saveData(getApplicationContext(), mUrlRequestForJsonFavorites);
    }

    private void loadDataIntoFavoriteList() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        mUrlRequestForJsonFavorites = sharedPreferences.getString(URL_JSON, "true");
        mTextViewFavoritesJson.setText(mUrlRequestForJsonFavorites);
    }

    /*todo:
    progressbar while image is loading picasso*/

    // Display date, explanation, Title and the image / video of the day
    // Default usage
    // date format is YYYY-MM-DD
    // https://api.nasa.gov/planetary/apod?api_key=XqN37uhbQmRUqsm2nTFk4rsugtM2Ibe0YUS9HDE3

    private void jsonParse() {
        mUrlRequestDefault = "https://api.nasa.gov/planetary/apod?api_key=XqN37uhbQmRUqsm2nTFk4rsugtM2Ibe0YUS9HDE3";


        if (mCurrentDateFormatInput != null){
            // Adding dates
            mUrlRequestForJson = mUrlRequestDefault + "&" + "date" + "=" + mCurrentDateFormatInput;
            Log.d(TAG, "mUrlRequestForJson: " + mUrlRequestForJson);
        }
        else {
            mUrlRequestForJson = mUrlRequestDefault;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, mUrlRequestForJson, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String explanation = response.getString("explanation");
                            String date = response.getString("date");
                            String title = response.getString("title");
                            String imageUrl = response.getString("url");
                            mTextViewTitle.setText(title);
                            mTextViewMetadataDate.setText("Taken on: " + date);
                            mTextViewExplanation.setText(explanation);
                            Log.d(TAG, "Image url: " + imageUrl);

                            // Attempt to resize the image to fit exactly into the target
                            Picasso.get().load(imageUrl).fit().into(imageView);
                        } catch (JSONException e) {
                            Log.d(TAG, "JSONException: ");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "JsonObject Error Response" + error);
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(50, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }

    // Set Date here: YY-MM-DD
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Log.d(TAG, "onDateSet: " + year + ":" + (month + 1) + ":" + dayOfMonth);
        mCurrentDateString = DateFormat.getDateInstance().format(c.getTime());
        Log.d(TAG, "currentDateString: " + mCurrentDateString);

        String monthString = ((month + 1) / 10 <= 1) ? Integer.toString(month + 1) : "0" + (month + 1);
        String dayOfMonthString = ((dayOfMonth) / 10 >= 1) ? Integer.toString(dayOfMonth) : "0" + (dayOfMonth);
        mCurrentDateFormatInput = year + "-" + (monthString) + "-" + (dayOfMonthString);
        Log.d(TAG, "currentDateFormatInput: " + mCurrentDateFormatInput);

        mTextViewDate.setText(mCurrentDateString);
    }

    private void openRecyclerViewActivity() {
        Intent intent = new Intent(this, ExampleRecyclerViewActivity.class);
        startActivity(intent);
    }
}