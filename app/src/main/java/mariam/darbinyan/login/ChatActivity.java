package mariam.darbinyan.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import mariam.darbinyan.login.BuildConfig;
import androidx.core.content.ContextCompat;

public class ChatActivity extends AppCompatActivity {

    private TextView chatResponse;
    private EditText userInput;
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatResponse = findViewById(R.id.chatResponse);
        userInput = findViewById(R.id.userInput);
        Button sendBtn = findViewById(R.id.sendBtn);

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY);
        model = GenerativeModelFutures.from(gm);

        // 1. Prepare the bitmap variable
        final android.graphics.Bitmap[] capturedBitmap = {null};
        String base64Image = getIntent().getStringExtra("image_data");

        if (base64Image != null) {
            byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
            capturedBitmap[0] = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }

        // 2. Set the listener ONCE
        sendBtn.setOnClickListener(v -> {
            String query = userInput.getText().toString();
            if (query.isEmpty()) return;

            if (capturedBitmap[0] != null) {
                askAIWithImage(query, capturedBitmap[0]);
            } else {
                askAI(query);
            }
        });
    }

    private void askAIWithImage(String text, android.graphics.Bitmap userImage) {
        chatResponse.setText("Thinking...");

        // Add the "Stylist" persona to the text
        String stylistPrompt = "Act as a professional fashion stylist. Give me a very short, chic, and actionable answer: " + text;

        com.google.ai.client.generativeai.type.Content content =
                new com.google.ai.client.generativeai.type.Content.Builder()
                        .addImage(userImage)
                        .addText(stylistPrompt)
                        .build();

        com.google.common.util.concurrent.ListenableFuture<com.google.ai.client.generativeai.type.GenerateContentResponse> response =
                model.generateContent(content);

        com.google.common.util.concurrent.Futures.addCallback(response, new com.google.common.util.concurrent.FutureCallback<com.google.ai.client.generativeai.type.GenerateContentResponse>() {
            @Override
            public void onSuccess(com.google.ai.client.generativeai.type.GenerateContentResponse result) {
                runOnUiThread(() -> chatResponse.setText(result.getText()));
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> chatResponse.setText("AI Error: " + t.getMessage()));
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void askAI(String text) {
        chatResponse.setText("Thinking...");

        // Add the "Stylist" persona here too
        String stylistPrompt = "Act as a professional fashion stylist. Give me a very short, chic, and actionable answer: " + text;

        Content content = new Content.Builder().addText(stylistPrompt).build();
        ListenableFuture<com.google.ai.client.generativeai.type.GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<com.google.ai.client.generativeai.type.GenerateContentResponse>() {
            @Override
            public void onSuccess(com.google.ai.client.generativeai.type.GenerateContentResponse result) {
                runOnUiThread(() -> chatResponse.setText(result.getText()));
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> chatResponse.setText("Error: " + t.getMessage()));
            }
        }, ContextCompat.getMainExecutor(this));
    }
}