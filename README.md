# 📸 Ringo — Android Photo Editor

An open-source, lightweight Android photo editing application built with Java and Android Studio. Ringo provides real-time color matrix filters, precise image adjustment controls, pixel-level post-processing (sharpening & noise/grain), and aspect-ratio cropping — all packaged in a clean, dark-mode native interface.

---

## ✨ Features

- **🎨 Preset Filters:**
  - Fast, real-time matrix presets: *Vintage*, *Nostalgia*, *Juno*, *Mexico*, *B&W*, *Invert*, and *Sepia*.

- **🎛️ Detailed Adjustments:**
  - Live slider controls for **Brightness**, **Contrast**, **Exposure**, **Saturation**, **Vibrance**, and **Tint** (Green/Magenta balance).

- **🔍 Pixel-Level Processing:**
  - **Sharpening:** Custom edge-enhancement convolution algorithm.
  - **Noise / Film Grain:** Dynamic pixel noise generator for a retro aesthetic.

- **✂️ Aspect Ratio Cropping:**
  - Presets for **Free**, **1:1 Square**, **4:3**, and **16:9** aspect ratios with dynamic dimension calculation.

- **💾 Save & Export:**
  - Exports edited high-resolution images straight to `Pictures/Ringo` via Android's `MediaStore` API.

---

## 🛠️ Built With

- **Language:** Java
- **UI Design:** Native Android XML (`FrameLayout`, `LinearLayout`, `HorizontalScrollView`)
- **Graphics Pipeline:** Android `Canvas`, `ColorMatrix`, `ColorMatrixColorFilter`, and Bitmap Pixel Manipulation
- **Target SDK:** Android 10+ (API 29+) with legacy storage compatibility

---

## 👨‍💻 Author

Created with ⚡ by **cluck5170**
