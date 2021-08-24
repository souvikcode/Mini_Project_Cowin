package com.project.vaccinenotifier;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmail, etPassword, etConfirmPassword, etAge;
    private Spinner spState, spDistrict;
    private Button bRegister;

    private ProgressDialog dialog;

    private FirebaseAuth mAuth;

    private JSONArray states, districts;
    private String[] states_array, districts_array;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Register");

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etPasswordConfirm);
        etAge = findViewById(R.id.etAge);
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        bRegister = findViewById(R.id.bRegister);

        bRegister.setOnClickListener(this);

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Loading");
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();

        requestQueue = Volley.newRequestQueue(this);

        loadStates();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRegister:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmpassword = etConfirmPassword.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        int statePosition = spState.getSelectedItemPosition();
        int districtPosition = spDistrict.getSelectedItemPosition();

        if (email.isEmpty()) {
            etEmail.setError("Please write Email");
            etEmail.requestFocus();

            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please write Password");
            etPassword.requestFocus();

            return;
        }

        if (confirmpassword.isEmpty()) {
            etConfirmPassword.setError("Please write Retype Password");
            etConfirmPassword.requestFocus();

            return;
        }

        if (age.isEmpty()) {
            etAge.setError("Please write Age");
            etAge.requestFocus();

            return;
        }

        if (statePosition == 0) {
            Toast.makeText(this, "Please select State", Toast.LENGTH_SHORT).show();
            spState.requestFocus();

            return;
        }

        if (districtPosition == 0) {
            Toast.makeText(this, "Please select District", Toast.LENGTH_SHORT).show();
            spDistrict.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please write valid Email");
            etEmail.requestFocus();

            return;
        }

        if (!password.equals(confirmpassword)) {
            Toast.makeText(this, "Passwords are not same. Please try again.", Toast.LENGTH_SHORT).show();

            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 characters.", Toast.LENGTH_SHORT).show();

            return;
        }

        dialog.show();

        try {
            String stateName = states.getJSONObject(statePosition - 1).getString("state_name");
            int stateID = states.getJSONObject(statePosition - 1).getInt("state_id");
            String districtName = districts.getJSONObject(districtPosition - 1).getString("district_name");
            int districtID = districts.getJSONObject(districtPosition - 1).getInt("district_id");

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                User user = new User(email, Integer.parseInt(age), stateName, districtName, stateID, districtID);

                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "User has been registered Successfullly", Toast.LENGTH_SHORT).show();

                                            dialog.dismiss();

                                            onBackPressed();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Failed to register! Try Again!!", Toast.LENGTH_SHORT).show();

                                            dialog.dismiss();
                                        }
                                    }
                                });
                            } else {
                                Log.e("--", task.toString());
                                Toast.makeText(RegisterActivity.this, "Failed to register! Try Again!!", Toast.LENGTH_SHORT).show();

                                dialog.dismiss();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStates() {
        dialog.show();

        String url = "https://cdn-api.co-vin.in/api/v2/admin/location/states";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            JSONObject object = response;
                            states = object.getJSONArray("states");

                            states_array = new String[states.length() + 1];
                            states_array[0] = "Select State";
                            for(int i = 0; i < states.length(); i++) {
                                states_array[i+1] = states.getJSONObject(i).getString("state_name");
                            }

                            ArrayAdapter adapterStates = new ArrayAdapter<String>(RegisterActivity.this, R.layout.spinner_item, states_array);
                            spState.setAdapter(adapterStates);

                            spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    if (i != 0) {
                                        loadDistrict();

                                        spDistrict.setVisibility(View.VISIBLE);
                                    } else {
                                        spDistrict.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();

                        Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


        requestQueue.add(jsonObjectRequest);
    }

    private void loadDistrict() {
        try {
            dialog.show();

            int stateid = states.getJSONObject(spState.getSelectedItemPosition() - 1).getInt("state_id");
            String url = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/" + stateid;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            dialog.dismiss();
                            try {
                                JSONObject object = response;
                                districts = object.getJSONArray("districts");

                                districts_array = new String[districts.length() + 1];
                                districts_array[0] = "Select District";
                                for (int i = 0; i < districts.length(); i++) {
                                    districts_array[i + 1] = districts.getJSONObject(i).getString("district_name");
                                }

                                ArrayAdapter adapterDistricts = new ArrayAdapter<String>(RegisterActivity.this, R.layout.spinner_item, districts_array);
                                spDistrict.setAdapter(adapterDistricts);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();

                            Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
