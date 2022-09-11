package com.example.doctorapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.doctorapp.utils.DateUtils;
import com.example.doctorapp.utils.SharedPrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    String patientName;
    String[] medicalRecordArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.linear);
        linearLayout.setVisibility(View.GONE);

        patientName = "";
    }

    public void scanClicked(View view) {
        try {

            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            startActivityForResult(intent, 0);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                patientName = data.getStringExtra("SCAN_RESULT");
                try {
                    String docName = SharedPrefUtils.getUserFromSP(this);
                    linearLayout.setVisibility(View.VISIBLE);
                    fetchAllMedicalRecord(patientName, docName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }

    public void viewClicked(View view) {
        if (!patientName.equals("")) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View promptView = layoutInflater.inflate(R.layout.dialog_view_records, null);
            final AlertDialog alertD = new AlertDialog.Builder(this).create();
            alertD.setTitle("Patient's Medical Record");

            ArrayAdapter adapter = new ArrayAdapter<String>(this,
                    R.layout.list_item_medical_record, medicalRecordArray);

            ListView listView = (ListView) promptView.findViewById(R.id.listView);
            listView.setAdapter(adapter);

            Button back = (Button) promptView.findViewById(R.id.back);

            back.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    alertD.cancel();
                }
            });
            alertD.setView(promptView);
            alertD.show();
        }
    }

    private void fetchAllMedicalRecord(String user, String docName) throws IOException {
        String url = "https://e0f8wiau03-e0xgy8n04x-connect.de0-aws-ws.kaleido.io/query";
        String jsonBody = "{\n" +
                "  \"headers\": {\n" +
                "    \"signer\": \"" + docName + "\",\n" +
                "    \"channel\": \"default-channel\",\n" +
                "    \"chaincode\": \"asset_transfer\"\n" +
                "  },\n" +
                "  \"func\": \"ReadAsset\",\n" +
                "  \"args\": [\n" +
                "    \"" + user + "\"\n" +
                "  ],\n" +
                "  \"strongread\": true\n" +
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

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = new JSONObject(myResponse);
                            String medicalRecord = json.getJSONObject("result").getString("MedicalRecord");

                            JSONArray medicalRecordJson = new JSONArray(medicalRecord);
                            ArrayList<String> list = new ArrayList<String>();
                            for (int i = 0; i < medicalRecordJson.length(); i++) {
                                JSONObject jsonObject = new JSONObject(medicalRecordJson.get(i).toString());;
                                String data = "Date: " + jsonObject.getString("date")
                                        + "\nDoctor Name: " + jsonObject.getString("author")
                                        + "\nPrescription: " + jsonObject.getString("prescription");
                                list.add(data);
                            }
                            medicalRecordArray = list.toArray(new String[list.size()]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    public void addClicked(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_add_records, null);
        final AlertDialog alertD = new AlertDialog.Builder(this).create();
        alertD.setTitle("Add Medical Record");

        EditText presEditText = (EditText) promptView.findViewById(R.id.name);

        Button back = (Button) promptView.findViewById(R.id.back);
        Button save = (Button) promptView.findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!patientName.equals("")) {
                    String docName = SharedPrefUtils.getUserFromSP(MainActivity.this);
                    String today = DateUtils.getToday();
                    String pres = presEditText.getText().toString();
                    String url = "https://e0f8wiau03-e0xgy8n04x-connect.de0-aws-ws.kaleido.io/transactions";
                    String jsonBody = "{\n" +
                            "  \"headers\": {\n" +
                            "    \"type\": \"SendTransaction\",\n" +
                            "    \"signer\": \"" + docName + "\",\n" +
                            "    \"channel\": \"default-channel\",\n" +
                            "    \"chaincode\": \"asset_transfer\"\n" +
                            "  },\n" +
                            "  \"func\": \"UpdateMedicalRecordAsset\",\n" +
                            "  \"args\": [\n" +
                            "    \"" + patientName + "\", \"{\\\"date\\\":\\\"" + today + "\\\",\\\"author\\\":\\\"" + docName + "\\\",\\\"prescription\\\":\\\"" + pres + "\\\"}\"\n" +
                            "  ],\n" +
                            "  \"init\": false\n" +
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

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Medical Record Added", Toast.LENGTH_LONG).show();
                                    alertD.cancel();
                                }
                            });

                        }
                    });
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertD.cancel();
            }
        });
        alertD.setView(promptView);
        alertD.show();
    }

    public void refreshClicked(View view) {
        if (!patientName.equals("")) {
            try {
                String docName = SharedPrefUtils.getUserFromSP(this);
                fetchAllMedicalRecord(patientName, docName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}