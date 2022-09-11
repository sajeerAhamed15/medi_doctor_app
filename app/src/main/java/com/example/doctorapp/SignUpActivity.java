package com.example.doctorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.doctorapp.utils.SharedPrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    TextView email;
    TextView password;
    TextView name;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        progressBar = findViewById(R.id.loading);

        progressBar.setVisibility(View.INVISIBLE);
    }

    public void createAccountClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        String _name = name.getText().toString().replaceAll("\\s","");
        String _contact = email.getText().toString().replaceAll("\\s","");
        String _password = password.getText().toString();

        // send an api request to create a new doctor
        createNewDoctor(_name);
    }

    private void createNewDoctor(String name) {
        // send http request
        try {
            String url = "https://e0f8wiau03-e0xgy8n04x-connect.de0-aws-ws.kaleido.io/identities";
            String jsonBody = "{\n" +
                    "  \"name\": \"" + name + "\",\n" +
                    "  \"type\": \"client\"\n" +
                    "}";

            OkHttpClient client = new OkHttpClient();

            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Basic ZTBwOHlpY2R0ZDpUbTlfYmdNWHR3N2F0ZGF3V0pSVFNKaWtHWEhDNF9TZTM3S28wUFZxaEZZ")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String myResponse = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = new JSONObject(myResponse);
                                String secret = json.getString("secret");

                                enrollDoctor(name, secret);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enrollDoctor(String name, String secret) {
        // send http request
        try {
            String url = "https://e0f8wiau03-e0xgy8n04x-connect.de0-aws-ws.kaleido.io/identities/" + name + "/enroll";
            String jsonBody = "{\n" +
                    "  \"secret\": \"" + secret + "\"\n" +
                    "}";

            OkHttpClient client = new OkHttpClient();

            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Basic ZTBwOHlpY2R0ZDpUbTlfYmdNWHR3N2F0ZGF3V0pSVFNKaWtHWEhDNF9TZTM3S28wUFZxaEZZ")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String myResponse = response.body().string();

                    // save user name in shared pref and send to main activity
                    SharedPrefUtils.saveUserInSP(name, SignUpActivity.this);
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void loginClicked(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}