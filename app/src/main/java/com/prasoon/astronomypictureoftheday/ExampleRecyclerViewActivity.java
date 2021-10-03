package com.prasoon.astronomypictureoftheday;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import static com.prasoon.astronomypictureoftheday.MainActivity.mFavoriteList;

public class ExampleRecyclerViewActivity extends AppCompatActivity implements ExampleAdaptor.OnItemClickListener {
    private String TAG = "ExampleRecyclerViewActivity";

    public static final String EXTRA_URL = "imageUrl";
    public static final String EXTRA_TITLE = "creatorName";
    public static final String EXTRA_DATE = "likeCount";

    private RecyclerView mRecyclerView;
    private ExampleAdaptor mExampleAdaptor;
    private ArrayList<ExampleItem> mExampleItemList;
    private RequestQueue mRequestQueue;
    protected static String mUrlRequestForJsonRemoveFavorites = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_recycler_view);

        mRecyclerView = findViewById(R.id.recyclerView);
        // Don't change width and height and load all images in fixed view
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mExampleItemList = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);

        mFavoriteList = PrefConfig.readListFromPref(getApplicationContext());
        if (mFavoriteList == null){
            mFavoriteList = new HashSet<>();
        }

        parseJSON();
    }

    private void parseJSON() {
        for (String requestJSON : mFavoriteList) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestJSON, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String date = response.getString("date");
                                String title = response.getString("title");
                                String imageUrl = response.getString("url");
                                String[] values = date.split("-");
                                int day = Integer.parseInt(values[0]);
                                int month = Integer.parseInt(values[1]);
                                int year = Integer.parseInt(values[2]);
                                mUrlRequestForJsonRemoveFavorites = requestJSON;
                                Log.d(TAG, "requestJSON: " + requestJSON);
                                mExampleItemList.add(new ExampleItem(imageUrl, title, date));
                                // fill adaptor with the data
                                mExampleAdaptor = new ExampleAdaptor(ExampleRecyclerViewActivity.this, mExampleItemList);
                                mRecyclerView.setAdapter(mExampleAdaptor);
                                mExampleAdaptor.setOnItemClickListener(ExampleRecyclerViewActivity.this);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ExampleRecyclerViewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            error.printStackTrace();
                        }
                    });
            mRequestQueue.add(request);
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        ExampleItem clickedItem = mExampleItemList.get(position);
        detailIntent.putExtra(EXTRA_URL, clickedItem.getImageUrl());
        detailIntent.putExtra(EXTRA_TITLE, clickedItem.getTitle());
        detailIntent.putExtra(EXTRA_DATE, clickedItem.getDate());
        startActivity(detailIntent);
    }

    @Override
    public void onDeleteItem(int position) {
        removeItem(position);
        mFavoriteList.remove(mUrlRequestForJsonRemoveFavorites);
        PrefConfig.updateData(getApplicationContext());
    }

    private void removeItem(int position) {
        mExampleItemList.remove(position);
        mExampleAdaptor.notifyItemRemoved(position);
    }
}