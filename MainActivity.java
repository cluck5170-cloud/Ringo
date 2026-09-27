package com.example.ringo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int PICK_PHOTO_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPickImage = findViewById(R.id.btnPickImage);
        if (btnPickImage != null) {
            btnPickImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPhotoGallery();
                }
            });
        }
    }

    private void openPhotoGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        
        // Strict photo types (excluding video and gifs)
        String[] allowedMimeTypes = {
            "image/jpeg", 
            "image/png", 
            "image/webp", 
            "image/bmp"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, allowedMimeTypes);

        try {
            startActivityForResult(intent, PICK_PHOTO_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open gallery picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedPhotoUri = data.getData();

            String mimeType = getContentResolver().getType(selectedPhotoUri);
            if (mimeType != null && mimeType.toLowerCase().contains("gif")) {
                Toast.makeText(this, "GIFs are not supported! Please select a static photo.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent editorIntent = new Intent(MainActivity.this, EditorActivity.class);
            editorIntent.putExtra("photoUri", selectedPhotoUri.toString());
            startActivity(editorIntent);
        }
    }
}
