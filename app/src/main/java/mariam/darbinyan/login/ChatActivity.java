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

        // Initialize Gemini
        // Use the specific model ID and define the model version explicitly
        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                "AIzaSyCqiuYyaMib3Yn_BZmIEBxszuoKwUsyKcY"
        );
        model = GenerativeModelFutures.from(gm);

        sendBtn.setOnClickListener(v -> {
            String query = userInput.getText().toString();
            if (!query.isEmpty()) {
                askAI(query);
            }
        });
        String base64Image = getIntent().getStringExtra("image_data");

        if (base64Image != null) {
            // 1. Convert the string back into a Bitmap (the AI needs the bitmap)
            byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
            android.graphics.Bitmap capturedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            // 2. Set up the Send button to use the image version of the AI
            sendBtn.setOnClickListener(v -> {
                String query = userInput.getText().toString();
                if (!query.isEmpty()) {
                    askAIWithImage(query, capturedBitmap);
                }
            });
        }
    }

    private void askAIWithImage(String text, android.graphics.Bitmap userImage) {
        chatResponse.setText("The AI is looking at your outfit...");

        // Gemini "Content" can hold both Text and Bitmaps
        com.google.ai.client.generativeai.type.Content content =
                new com.google.ai.client.generativeai.type.Content.Builder()
                        .addImage(userImage)
                        .addText(text)
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
        }, this.getMainExecutor());
    }

    private void askAI(String text) {
        chatResponse.setText("Thinking...");

        Content content = new Content.Builder().addText(text).build();
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
        }, this.getMainExecutor());
    }
}