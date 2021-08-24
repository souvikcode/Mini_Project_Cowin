package com.project.vaccinenotifier;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDate;
    private Button bChangeDate;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ProgressDialog dialog;

    private FirebaseUser user;
    private String userID;
    private Query query;
    private User userMain;

    private SimpleDateFormat df;
    private Calendar c;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Vaccination Centers");

        tvDate = findViewById(R.id.tvDate);
        bChangeDate = findViewById(R.id.bChangeDate);
        recyclerView = findViewById(R.id.recycleView);

        bChangeDate.setOnClickListener(this);

        //get current date
        c = Calendar.getInstance();
        df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c.getTime());
        tvDate.setText(formattedDate);

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Loading");
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        requestQueue = Volley.newRequestQueue(this);

        dialog.show();
        query = FirebaseDatabase.getInstance().getReference("Users").orderByKey().equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("users----", "" + snapshot.getChildrenCount());

                for(DataSnapshot data : snapshot.getChildren()) {
                    dialog.dismiss();

                    userMain = data.getValue(User.class);

                    loadCenters();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void loadCenters() {
        dialog.show();

        String url = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByDistrict?district_id=" + userMain.getDistrict_id() + "&date=" + tvDate.getText().toString();
        Log.e("--", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            JSONObject object = response;
                            JSONArray sessions = object.getJSONArray("sessions");

                            JSONArray sessions_age = new JSONArray();
                            Log.e("original", "" + sessions.length());

                            if(userMain.age >= 18 && userMain.age <= 44) {
                                for(int i = 0; i < sessions.length(); i++) {
                                    JSONObject temp = sessions.getJSONObject(i);

                                    if(temp.getInt("min_age_limit") == 18) {
                                        sessions_age.put(temp);
                                    }
                                }
                            } else {
                                if(userMain.age >= 45) {
                                    for(int i = 0; i < sessions.length(); i++) {
                                        JSONObject temp = sessions.getJSONObject(i);

                                        if(temp.getInt("min_age_limit") == 45) {
                                            sessions_age.put(temp);
                                        }
                                    }
                                }
                            }
                            Log.e("age", "" + sessions_age.length());

                            adapter = new RowAdapter(sessions_age);
                            recyclerView.setAdapter(adapter);
                            
                            if(sessions.length() == 0) {
                                Toast.makeText(MainActivity.this, "No Vaccine Centers Available", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();

                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        jsonObjectRequest.setShouldCache(false);
        requestQueue.add(jsonObjectRequest);

        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                requestQueue.getCache().clear();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bChangeDate:
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth, 0, 0, 0);

                        String chosenDate = df.format(cal.getTime());
                        tvDate.setText(chosenDate);

                        loadCenters();
                    }
                }, mYear, mMonth, mDay);

                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                datePickerDialog.show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.custom_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();

                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                finish();
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}