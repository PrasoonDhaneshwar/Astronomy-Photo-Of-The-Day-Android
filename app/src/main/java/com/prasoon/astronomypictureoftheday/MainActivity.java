package com.prasoon.astronomypictureoftheday;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
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

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String URL_JSON = "urlJson";
    public static final String NIGHT_MODE = "nightMode";

    private TextView mTextViewExplanation;
    private TextView mTextViewDatePicker;
    private TextView mTextViewSuggest;
    private TextView mTextViewTitle;
    private TextView mTextViewMetadataDate;

    private Button mButtonParse;
    private Button mButtonSelectDateButton;
    private Button mButtonDarkMode;

    private ImageView mImageView;
    private ImageView mImageViewSelectSecondActivity;
    private ImageView mImageViewAddIntoFavorites;

    private RequestQueue mQueue;
    private String mCurrentDateString = null;
    private String mCurrentDateFormatInput = null;
    private String mUrlRequestForJson = null;
    private static final String mUrlRequestDefaultKey = "https://api.nasa.gov/planetary/apod?api_key=XqN37uhbQmRUqsm2nTFk4rsugtM2Ibe0YUS9HDE3";
    private boolean isNightModeOn = false;

    // This value will be given to RecyclerviewList for maintaining favorites
    protected static String mUrlRequestForJsonFavorites = null;
    // This value will be used when user opens the app again and corresponding date will be shown
    protected static String mUrlRequestForJsonLastActive = null;

    protected void setmFavoriteList(String jsonLink) {
        mFavoriteList.add(jsonLink);
    }

    protected static Set<String> mFavoriteList = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Dark mode preferences
        mButtonDarkMode = findViewById(R.id.darkMode);
        SharedPreferences prefDarkMode = getSharedPreferences(NIGHT_MODE, MODE_PRIVATE);
        SharedPreferences.Editor editorDarkMode = prefDarkMode.edit();
        isNightModeOn = prefDarkMode.getBoolean(NIGHT_MODE, false);

        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            mButtonDarkMode.setText("Light Mode");
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            mButtonDarkMode.setText("Dark Mode");
        }
        // Start from the last opened date
        mUrlRequestForJsonLastActive = PrefConfig.retrieveLastRequest(getApplicationContext());


        mButtonDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNightModeOn) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editorDarkMode.putBoolean(NIGHT_MODE, false);
                    mButtonDarkMode.setText("Light Mode");
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editorDarkMode.putBoolean(NIGHT_MODE, true);
                    mButtonDarkMode.setText("Dark Mode");
                }
                editorDarkMode.apply();
            }
        });

        mTextViewExplanation = findViewById(R.id.textViewExplanation);
        mTextViewExplanation.setMovementMethod(new ScrollingMovementMethod());
        mTextViewDatePicker = findViewById(R.id.textViewDatePicker);
        mTextViewSuggest = findViewById(R.id.textViewSuggest);
        mButtonParse = findViewById(R.id.buttonParse);
        mImageView = findViewById(R.id.imageViewResult);

        // Open Favorites Activity
        mImageViewSelectSecondActivity = findViewById(R.id.selectSecondActivity);
        mImageViewSelectSecondActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecyclerViewActivity();
            }
        });

        mTextViewTitle = findViewById(R.id.textViewTitle);
        mTextViewMetadataDate = findViewById(R.id.textViewMetadataDate);

        // Initialize with a new request when app is opened.
        mQueue = Volley.newRequestQueue(this);
        jsonParse();

        mButtonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextViewSuggest.setText("");
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

        mImageViewAddIntoFavorites = findViewById(R.id.addIntoFavorites);
        mImageViewAddIntoFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataIntoFavoriteList();
            }
        });

        loadDataIntoFavoriteList();
        mTextViewDatePicker.setText("\"Select Date\" to get today's picture!");
    }

    // Shared preferences methods
    private void saveDataIntoFavoriteList() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        mUrlRequestForJsonFavorites = mUrlRequestForJson;
        mFavoriteList.add(mUrlRequestForJsonFavorites);

        setmFavoriteList(mUrlRequestForJsonFavorites);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(URL_JSON, mUrlRequestForJson);
        Log.d(TAG, "mUrlRequestForJsonFavorites: " + mUrlRequestForJsonFavorites);
        editor.apply();
        PrefConfig.saveData(getApplicationContext(), mUrlRequestForJsonFavorites);
        Toast.makeText(MainActivity.this, "Added to Favorites!", Toast.LENGTH_SHORT).show();
    }

    private void loadDataIntoFavoriteList() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        mUrlRequestForJsonFavorites = sharedPreferences.getString(URL_JSON, "true");
        Log.d(TAG, "mUrlRequestForJsonFavorites: " + mUrlRequestForJsonFavorites);
    }

    // Display date, explanation, Title and the image / video of the day
    // Default usage
    // date format is YYYY-MM-DD
    // https://api.nasa.gov/planetary/apod?api_key=XqN37uhbQmRUqsm2nTFk4rsugtM2Ibe0YUS9HDE3

    private void jsonParse() {
        Log.d(TAG, "mUrlRequestForJsonLastActive: " + mUrlRequestForJsonLastActive);

        if (mCurrentDateFormatInput != null) {
            // Adding dates
            mUrlRequestForJson = mUrlRequestDefaultKey + "&" + "date" + "=" + mCurrentDateFormatInput;
            mUrlRequestForJsonLastActive = mUrlRequestDefaultKey + "&" + "date" + "=" + mCurrentDateFormatInput;
        } else {
            // If the Last Active value is null.
            if (mUrlRequestForJsonLastActive.equals("") || mUrlRequestForJsonLastActive == null) {
                mUrlRequestForJson = mUrlRequestDefaultKey;
            } else {
                mUrlRequestForJson = mUrlRequestForJsonLastActive;
            }
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, mUrlRequestForJson, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "mUrlRequestForJson: " + mUrlRequestForJson);
                            String explanation = response.getString("explanation");
                            String date = response.getString("date");
                            String title = response.getString("title");
                            String imageUrl = response.getString("url");
                            mTextViewTitle.setText(title);
                            mTextViewMetadataDate.setText("Taken on: " + date);
                            mTextViewExplanation.setText(explanation);
                            Log.d(TAG, "Image url: " + imageUrl);

                            // Attempt to resize the image to fit exactly into the target
                            if (imageUrl == null) {
                                Toast.makeText(MainActivity.this, "Image not received. Try again.", Toast.LENGTH_SHORT).show();
                            }
                            Picasso.get().load(imageUrl).into(mImageView);
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
                        if (error instanceof TimeoutError) {
                            Toast.makeText(MainActivity.this, "Unable to fetch from APOD. Try again.", Toast.LENGTH_SHORT).show();
                            mButtonParse.setEnabled(true);
                        } else if (error instanceof NoConnectionError) {
                            Toast.makeText(MainActivity.this, "Please connect to Internet and try again.", Toast.LENGTH_SHORT).show();
                            mButtonParse.setEnabled(false);
                        }
                        error.printStackTrace();
                        mButtonSelectDateButton.setEnabled(false);
                        mButtonDarkMode.setEnabled(false);
                        mImageViewSelectSecondActivity.setEnabled(false);
                        mImageViewAddIntoFavorites.setEnabled(false);
                        mTextViewExplanation.setText("No Internet!");
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

        mTextViewDatePicker.setText(mCurrentDateString);
        mTextViewSuggest.setText("Click here to \"View\"");
    }

    private void openRecyclerViewActivity() {
        Intent intent = new Intent(this, ExampleRecyclerViewActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Saving last request: " + mUrlRequestForJsonLastActive);
        PrefConfig.saveLastActive(getApplicationContext(), mUrlRequestForJsonLastActive);
        super.onDestroy();
    }
}