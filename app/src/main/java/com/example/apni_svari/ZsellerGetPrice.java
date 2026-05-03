package com.example.apni_svari;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ZsellerGetPrice extends Fragment {

<<<<<<< HEAD
    // Gemini generateContent endpoint for text-only prompt requests.
    private static final String MODEL_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    // Replace with your real key (or move to BuildConfig/local.properties for production).
    private static final String API_KEY = "YOUR_API_KEY";
=======
    private static final String MODEL_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
>>>>>>> 9861fa2ee86dcc64a2ee0e8616a18871676c85ad

    private EditText name, model, condition, price;
    private TextView result;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_page, container, false);

        name = view.findViewById(R.id.carNameInput);
        model = view.findViewById(R.id.carModelInput);
        condition = view.findViewById(R.id.carConditionInput);
        price = view.findViewById(R.id.carPriceInput);
        result = view.findViewById(R.id.resultText);
        Button btn = view.findViewById(R.id.getPriceBtn);

        btn.setOnClickListener(v -> {
            String prompt = "Give honest price suggestion in Pakistan in 3-4 lines:\n"
                    + "Car: " + name.getText().toString().trim() + "\n"
                    + "Model: " + model.getText().toString().trim() + "\n"
                    + "Condition: " + condition.getText().toString().trim() + "\n"
                    + "Expected Price: " + price.getText().toString().trim();

            callAPI(prompt);
        });

        return view;
    }

    private void callAPI(String prompt) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
<<<<<<< HEAD

                URL url = new URL(MODEL_URL + "?key=" + API_KEY);
=======
                String apiKey = BuildConfig.GEMINI_API_KEY;
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    postResult("Error: Gemini API key is not configured.");
                    return;
                }

                URL url = new URL(MODEL_URL + "?key=" + apiKey);
>>>>>>> 9861fa2ee86dcc64a2ee0e8616a18871676c85ad
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setDoOutput(true);

                JSONObject part = new JSONObject();
                part.put("text", prompt);

                JSONArray parts = new JSONArray();
                parts.put(part);

                JSONObject content = new JSONObject();
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                JSONObject body = new JSONObject();
                body.put("contents", contents);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                String responseText = readAll(stream);

                if (code < 200 || code >= 300) {
                    postResult("API Error (" + code + "): " + responseText);
                    return;
                }

                JSONObject obj = new JSONObject(responseText);
                JSONArray candidates = obj.optJSONArray("candidates");
                if (candidates == null || candidates.length() == 0) {
                    postResult("Error: No response candidates returned by the API.");
                    return;
                }

                JSONObject contentObj = candidates.getJSONObject(0).getJSONObject("content");
                JSONArray responseParts = contentObj.getJSONArray("parts");
                String text = responseParts.getJSONObject(0).getString("text");

                postResult(text);

            } catch (Exception e) {
                postResult("Error: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private void postResult(String message) {
        if (!isAdded()) {
            return;
        }
        requireActivity().runOnUiThread(() -> {
            if (result != null) {
                result.setText(message);
            }
        });
    }
}