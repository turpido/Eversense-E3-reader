package com.example.senddatatoserver;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class SupabaseAPI {

    String supabaseUrl = BuildConfig.SUPABASE_URL;
    String supabaseApiKey = BuildConfig.SUPABASE_ANON_KEY;
    // Specify the table name
    String table = "sugar";
    String errorTable = "error";
    String sugarCol = "sugar_val";
    String calibrateCol = "calibrate_needed";
    String tredludecCol = "tredludec_needed";
    String dateCol = "update_date";
    String TAG = "request";

    public String getSugarData() {
        return getData(sugarCol);
    }

    public String getCalibrateData() {
        return getData(calibrateCol);
    }

    public String getTredludecData() {
        return getData(tredludecCol);
    }

    public void updateSugarValue(int val) {
        updateCol(sugarCol, String.valueOf(val));
    }

    public void updateTredludec(Boolean val){
        updateCol(tredludecCol, String.valueOf(val));
    }
    public void updateCalibrate(Boolean val){
        updateCol(calibrateCol, String.valueOf(val));
    }

    public void updateError(String message, String function){
        insertError(message, function);
    }

    private String getData(String col) {
        StringBuilder result = new StringBuilder();
        Thread updateThread = new Thread((Runnable) () -> {
            try {
                // Specify your Supabase URL


                // Construct the URL for the HTTP GET request
                String apiUrl = String.format("%s/rest/v1/%s?select=%s&apikey=%s", supabaseUrl, table, col, supabaseApiKey);
                URL url = new URL(apiUrl);

                // Create connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("apikey", supabaseApiKey);

                // Get the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String json = response.toString();
                JSONArray jsonArray = new JSONArray(json);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String firstKey = (String) jsonObject.keys().next();
                result.append(jsonObject.getString(firstKey));
            } catch (Exception e) {
                Log.e("error", "error: " + e.getMessage());
                insertError(e.getMessage(), "getData");
            }
        });
        updateThread.start();

        try {
            updateThread.join();
        } catch (InterruptedException e) {
            insertError(e.getMessage(), "getData -> join");
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    private void updateCol(String updateColName, String updateValue) {
        Thread thread = new Thread((Runnable) () -> {
            try {
                String apiUrl = String.format("%s/rest/v1/%s?apikey=%s&%s=eq.%s", supabaseUrl, table, supabaseApiKey, "key", "9300f9d2-e31c-4064-9c4c-253e24043ec5");
                URL url = new URL(apiUrl);

                // Create connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Construct the JSON payload for the update
                String payload = String.format("{\"%s\": \"%s\",\"%s\": \"%s\"}", updateColName, updateValue, dateCol, new Date());

                // Write the payload to the connection output stream
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(payload);
                writer.flush();
                writer.close();

                connection.getResponseCode(); //for some reason required

            } catch (Exception e) {
                Log.e("error", "error update: " + e.getMessage());
                insertError(e.getMessage(), "updateCol");
            }
        });
        thread.start();
    }

    private void insertError(String message, String function) {
        Thread thread = new Thread((Runnable) () -> {
            try {
                String apiUrl = String.format("%s/rest/v1/%s?apikey=%s", supabaseUrl, errorTable, supabaseApiKey);
                URL url = new URL(apiUrl);

                // Create connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Construct the JSON payload for the update
                String messageClean = message.replaceAll("\"", "'");
                String payload = String.format("{\"key\": \"%s\",\"date\": \"%s\",\"message\": \"%s\",\"function\": \"%s\"}", UUID.randomUUID(), new Date(), messageClean, function);

                // Write the payload to the connection output stream
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(payload);
                writer.flush();
                writer.close();

                // Get the response code
                connection.getResponseCode(); //for some reason required
            } catch (Exception e) {
                Log.e("error", "error update: " + e.getMessage());
            }
        });
        thread.start();
    }
}
