package com.example.ringo;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Random;

public class EditorActivity extends Activity {

    private ImageView imageView;
    private Uri photoUri;
    private Bitmap baseBitmap;
    private Bitmap originalUncroppedBitmap;

    private View panelFilters;
    private View panelAdjust;
    private View panelCrop;
    private View panelDetails;
    private TextView txtImageInfo;

    private SeekBar seekBrightness;
    private SeekBar seekContrast;
    private SeekBar seekExposure;
    private SeekBar seekSaturation;
    private SeekBar seekVibrance;
    private SeekBar seekTint;
    private SeekBar seekSharpen;
    private SeekBar seekNoise;

    // Filter modes: 0=None, 1=BW, 2=Invert, 3=Sepia, 4=Vintage, 5=Nostalgia, 6=Juno, 7=Mexico
    private int selectedFilterMode = 0;

    // Selected aspect ratio for cropping (0 = Free, 1 = 1:1, 2 = 4:3, 3 = 16:9)
    private int selectedCropRatio = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        imageView = findViewById(R.id.imageView);
        panelFilters = findViewById(R.id.panelFilters);
        panelAdjust = findViewById(R.id.panelAdjust);
        panelCrop = findViewById(R.id.panelCrop);
        panelDetails = findViewById(R.id.panelDetails);
        txtImageInfo = findViewById(R.id.txtImageInfo);

        seekBrightness = findViewById(R.id.seekBrightness);
        seekContrast = findViewById(R.id.seekContrast);
        seekExposure = findViewById(R.id.seekExposure);
        seekSaturation = findViewById(R.id.seekSaturation);
        seekVibrance = findViewById(R.id.seekVibrance);
        seekTint = findViewById(R.id.seekTint);
        seekSharpen = findViewById(R.id.seekSharpen);
        seekNoise = findViewById(R.id.seekNoise);

        // Header Buttons
        findViewById(R.id.btnBackMenu).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveImage).setOnClickListener(v -> saveImage());
        findViewById(R.id.btnResetAll).setOnClickListener(v -> resetAllAdjustments());

        // Tab Switches
        findViewById(R.id.tabFilters).setOnClickListener(v -> switchTab(panelFilters));
        findViewById(R.id.tabAdjust).setOnClickListener(v -> switchTab(panelAdjust));
        findViewById(R.id.tabCrop).setOnClickListener(v -> switchTab(panelCrop));
        findViewById(R.id.tabDetails).setOnClickListener(v -> switchTab(panelDetails));

        // Filter Click Listeners
        findViewById(R.id.btnNone).setOnClickListener(v -> { selectedFilterMode = 0; applyCombinedEffects(); });
        findViewById(R.id.btnGray).setOnClickListener(v -> { selectedFilterMode = 1; applyCombinedEffects(); });
        findViewById(R.id.btnInvert).setOnClickListener(v -> { selectedFilterMode = 2; applyCombinedEffects(); });
        findViewById(R.id.btnSepia).setOnClickListener(v -> { selectedFilterMode = 3; applyCombinedEffects(); });
        findViewById(R.id.btnVintage).setOnClickListener(v -> { selectedFilterMode = 4; applyCombinedEffects(); });
        findViewById(R.id.btnNostalgia).setOnClickListener(v -> { selectedFilterMode = 5; applyCombinedEffects(); });
        findViewById(R.id.btnJuno).setOnClickListener(v -> { selectedFilterMode = 6; applyCombinedEffects(); });
        findViewById(R.id.btnMexico).setOnClickListener(v -> { selectedFilterMode = 7; applyCombinedEffects(); });

        // Crop Ratio Click Listeners
        findViewById(R.id.btnCropFree).setOnClickListener(v -> selectedCropRatio = 0);
        findViewById(R.id.btnCrop11).setOnClickListener(v -> selectedCropRatio = 1);
        findViewById(R.id.btnCrop43).setOnClickListener(v -> selectedCropRatio = 2);
        findViewById(R.id.btnCrop169).setOnClickListener(v -> selectedCropRatio = 3);
        findViewById(R.id.btnApplyCrop).setOnClickListener(v -> performCrop());

        // Sliders
        SeekBar.OnSeekBarChangeListener sliderListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                applyCombinedEffects();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        seekBrightness.setOnSeekBarChangeListener(sliderListener);
        seekContrast.setOnSeekBarChangeListener(sliderListener);
        seekExposure.setOnSeekBarChangeListener(sliderListener);
        seekSaturation.setOnSeekBarChangeListener(sliderListener);
        seekVibrance.setOnSeekBarChangeListener(sliderListener);
        seekTint.setOnSeekBarChangeListener(sliderListener);
        seekSharpen.setOnSeekBarChangeListener(sliderListener);
        seekNoise.setOnSeekBarChangeListener(sliderListener);

        String uriString = getIntent().getStringExtra("photoUri");
        if (uriString != null) {
            photoUri = Uri.parse(uriString);
            loadPhoto(photoUri);
        } else {
            Toast.makeText(this, "No photo loaded!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadPhoto(Uri uri) {
        try {
            originalUncroppedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            baseBitmap = originalUncroppedBitmap;
            imageView.setImageBitmap(baseBitmap);
            updateImageInfo();
            resetAllAdjustments();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateImageInfo() {
        if (baseBitmap != null) {
            txtImageInfo.setText("Resolution: " + baseBitmap.getWidth() + "x" + baseBitmap.getHeight());
        }
    }

    private void performCrop() {
        if (baseBitmap == null) return;

        int width = baseBitmap.getWidth();
        int height = baseBitmap.getHeight();

        int targetWidth = width;
        int targetHeight = height;

        // Calculate dimensions based on aspect ratios centered around the bitmap
        if (selectedCropRatio == 1) { // 1:1 Square
            int minDim = Math.min(width, height);
            targetWidth = minDim;
            targetHeight = minDim;
        } else if (selectedCropRatio == 2) { // 4:3
            if (width * 3 > height * 4) {
                targetHeight = height;
                targetWidth = (height * 4) / 3;
            } else {
                targetWidth = width;
                targetHeight = (width * 3) / 4;
            }
        } else if (selectedCropRatio == 3) { // 16:9
            if (width * 9 > height * 16) {
                targetHeight = height;
                targetWidth = (height * 16) / 9;
            } else {
                targetWidth = width;
                targetHeight = (width * 9) / 16;
            }
        } else { // Free crop default: inset 10% on each edge
            targetWidth = (int) (width * 0.8f);
            targetHeight = (int) (height * 0.8f);
        }

        int x = Math.max(0, (width - targetWidth) / 2);
        int y = Math.max(0, (height - targetHeight) / 2);

        baseBitmap = Bitmap.createBitmap(baseBitmap, x, y, targetWidth, targetHeight);
        updateImageInfo();
        applyCombinedEffects();
        Toast.makeText(this, "Cropped to " + targetWidth + "x" + targetHeight, Toast.LENGTH_SHORT).show();
    }

    private void applyCombinedEffects() {
        if (baseBitmap == null) return;

        // Process pixel-level transformations (Sharpen and Noise)
        Bitmap processedBitmap = baseBitmap;
        int sharpenVal = seekSharpen.getProgress();
        int noiseVal = seekNoise.getProgress();

        if (sharpenVal > 0 || noiseVal > 0) {
            processedBitmap = applyPixelLevelEffects(baseBitmap, sharpenVal, noiseVal);
        }
        
        imageView.setImageBitmap(processedBitmap);

        // Build ColorMatrix for fast realtime adjustments
        ColorMatrix combinedMatrix = new ColorMatrix();

        // 1. Preset Filter Matrix
        if (selectedFilterMode == 1) { // B&W
            ColorMatrix grayMatrix = new ColorMatrix();
            grayMatrix.setSaturation(0);
            combinedMatrix.postConcat(grayMatrix);
        } else if (selectedFilterMode == 2) { // Invert
            float[] invertArray = {
                -1f,  0f,  0f, 0f, 255f,
                 0f, -1f,  0f, 0f, 255f,
                 0f,  0f, -1f, 0f, 255f,
                 0f,  0f,  0f, 1f,   0f
            };
            combinedMatrix.postConcat(new ColorMatrix(invertArray));
        } else if (selectedFilterMode == 3) { // Sepia
            float[] sepiaArray = {
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f,     0f,     0f,     1f, 0f
            };
            combinedMatrix.postConcat(new ColorMatrix(sepiaArray));
        } else if (selectedFilterMode == 4) { // Vintage
            float[] vintageArray = {
                0.9f, 0.1f, 0.1f, 0f, 20f,
                0.1f, 0.8f, 0.1f, 0f, 15f,
                0.1f, 0.1f, 0.6f, 0f, 30f,
                0f,   0f,   0f,   1f, 0f
            };
            combinedMatrix.postConcat(new ColorMatrix(vintageArray));
        } else if (selectedFilterMode == 5) { // Nostalgia
            float[] nostalgiaArray = {
                1.1f, 0.0f, 0.0f, 0f, 10f,
                0.0f, 0.9f, 0.1f, 0f, 5f,
                0.1f, 0.0f, 0.8f, 0f, 20f,
                0f,   0f,   0f,   1f, 0f
            };
            combinedMatrix.postConcat(new ColorMatrix(nostalgiaArray));
        } else if (selectedFilterMode == 6) { // Juno
            float[] junoArray = {
                1.2f, 0.0f, 0.0f, 0f, -10f,
                0.0f, 1.2f, 0.0f, 0f, 5f,
                0.0f, 0.0f, 0.9f, 0f, -10f,
                0f,   0f,   0f,   1f, 0f
            };
            combinedMatrix.postConcat(new ColorMatrix(junoArray));
        } else if (selectedFilterMode == 7) { // Mexico
            float[] mexicoArray = {
                1.2f, 0.2f, 0.0f, 0f, 25f,
                0.1f, 1.1f, 0.0f, 0f, 20f,
                0.0f, 0.0f, 0.4f, 0f, -30f,
                0f,   0f,   0f,   1f, 0f
            };
            combinedMatrix.postConcat(new ColorMatrix(mexicoArray));
        }

        // 2. Brightness
        float brightness = seekBrightness.getProgress() - 100;
        float[] brightnessArray = {
            1f, 0f, 0f, 0f, brightness,
            0f, 1f, 0f, 0f, brightness,
            0f, 0f, 1f, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        };
        combinedMatrix.postConcat(new ColorMatrix(brightnessArray));

        // 3. Contrast
        float contrast = (seekContrast.getProgress() / 100f) + 0.5f;
        float translate = (-0.5f * contrast + 0.5f) * 255f;
        float[] contrastArray = {
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        };
        combinedMatrix.postConcat(new ColorMatrix(contrastArray));

        // 4. Exposure
        float exposure = seekExposure.getProgress() - 100;
        float expMult = (exposure >= 0) ? (1f + (exposure / 100f)) : (1f + (exposure / 200f));
        float[] exposureArray = {
            expMult, 0f, 0f, 0f, 0f,
            0f, expMult, 0f, 0f, 0f,
            0f, 0f, expMult, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        };
        combinedMatrix.postConcat(new ColorMatrix(exposureArray));

        // 5. Saturation
        float satVal = seekSaturation.getProgress() / 100f;
        ColorMatrix satMatrix = new ColorMatrix();
        satMatrix.setSaturation(satVal);
        combinedMatrix.postConcat(satMatrix);

        // 6. Vibrance
        float vibVal = (seekVibrance.getProgress() - 100) / 100f;
        float rx = 1.0f + (vibVal * 0.5f);
        float gx = 1.0f + (vibVal * 0.8f);
        float bx = 1.0f + (vibVal * 1.2f);
        float[] vibranceArray = {
            rx, 0f, 0f, 0f, 0f,
            0f, gx, 0f, 0f, 0f,
            0f, 0f, bx, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        };
        combinedMatrix.postConcat(new ColorMatrix(vibranceArray));

        // 7. Tint
        float tintShift = seekTint.getProgress() - 100;
        float redTint = (tintShift > 0) ? (tintShift * 0.5f) : 0f;
        float greenTint = (tintShift < 0) ? (-tintShift * 0.5f) : 0f;
        float blueTint = (tintShift > 0) ? (tintShift * 0.5f) : 0f;
        float[] tintArray = {
            1f, 0f, 0f, 0f, redTint,
            0f, 1f, 0f, 0f, greenTint,
            0f, 0f, 1f, 0f, blueTint,
            0f, 0f, 0f, 1f, 0f
        };
        combinedMatrix.postConcat(new ColorMatrix(tintArray));

        imageView.setColorFilter(new ColorMatrixColorFilter(combinedMatrix));
    }

    private Bitmap applyPixelLevelEffects(Bitmap src, int sharpenStrength, int noiseStrength) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        int[] outputPixels = new int[width * height];
        Random random = new Random();

        float sharpenWeight = sharpenStrength / 100f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int color = pixels[index];

                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                // Apply Sharpen Kernel
                if (sharpenStrength > 0 && x > 0 && x < width - 1 && y > 0 && y < height - 1) {
                    int top = pixels[(y - 1) * width + x];
                    int bottom = pixels[(y + 1) * width + x];
                    int left = pixels[y * width + (x - 1)];
                    int right = pixels[y * width + (x + 1)];

                    int edgeR = (r * 5) - Color.red(top) - Color.red(bottom) - Color.red(left) - Color.red(right);
                    int edgeG = (g * 5) - Color.green(top) - Color.green(bottom) - Color.green(left) - Color.green(right);
                    int edgeB = (b * 5) - Color.blue(top) - Color.blue(bottom) - Color.blue(left) - Color.blue(right);

                    r = (int) (r * (1 - sharpenWeight) + edgeR * sharpenWeight);
                    g = (int) (g * (1 - sharpenWeight) + edgeG * sharpenWeight);
                    b = (int) (b * (1 - sharpenWeight) + edgeB * sharpenWeight);
                }

                // Apply Noise
                if (noiseStrength > 0) {
                    int noise = (int) ((random.nextFloat() - 0.5f) * noiseStrength * 1.5f);
                    r += noise;
                    g += noise;
                    b += noise;
                }

                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                outputPixels[index] = Color.rgb(r, g, b);
            }
        }

        return Bitmap.createBitmap(outputPixels, width, height, Bitmap.Config.ARGB_8888);
    }

    private void resetAllAdjustments() {
        if (originalUncroppedBitmap != null) {
            baseBitmap = originalUncroppedBitmap;
        }
        selectedFilterMode = 0;
        selectedCropRatio = 0;
        seekBrightness.setProgress(100);
        seekContrast.setProgress(100);
        seekExposure.setProgress(100);
        seekSaturation.setProgress(100);
        seekVibrance.setProgress(100);
        seekTint.setProgress(100);
        seekSharpen.setProgress(0);
        seekNoise.setProgress(0);
        updateImageInfo();
        applyCombinedEffects();
    }

    private void switchTab(View activePanel) {
        panelFilters.setVisibility(View.GONE);
        panelAdjust.setVisibility(View.GONE);
        panelCrop.setVisibility(View.GONE);
        panelDetails.setVisibility(View.GONE);

        activePanel.setVisibility(View.VISIBLE);
    }

    private void saveImage() {
        if (imageView.getDrawable() == null) return;

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmapToSave = drawable.getBitmap();

        Bitmap resultBitmap = Bitmap.createBitmap(bitmapToSave.getWidth(), bitmapToSave.getHeight(), bitmapToSave.getConfig());
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(imageView.getColorFilter());
        canvas.drawBitmap(bitmapToSave, 0, 0, paint);

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "Ringo_Edit_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Ringo");
            }

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream outStream = getContentResolver().openOutputStream(uri);
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                if (outStream != null) outStream.close();
                Toast.makeText(this, "Saved to Pictures/Ringo!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
        }
    }
}
