# 🧳 Packers and Movers (Spring Boot + Python Integration)

This project is a hybrid **Spring Boot + Python** system designed to intelligently **match uploaded furniture images with IKEA product data** using image similarity algorithms.  
The tool is optimized for use in relocation, moving, or inventory platforms where identifying furniture dimensions from an image is needed.

The system uses Structural Similarity Index (SSIM) for image comparison, supports **category-based filtering**, and provides **fallback standard dimensions** for unmatched images.  
It generates a visual **HTML report** with estimated product details.

---

## 📁 Project Structure

MigrEase/
│
├── src/
│ └── main/
│ └── java/ # Java source files (Spring Boot)
│ └── resources/
│
├── python/
│ ├── app.py # 🧠 Main Python script for matching
│ ├── ikea.csv # 📦 IKEA product metadata (CSV with image links, dimensions, categories)
│ ├── image_cache_ssim/ # 🖼️ Cached images to speed up processing
│ ├── report/ # 📊 Auto-generated HTML reports
│
├── README.md # 📘 You're reading it
└── ...

yaml
Copy
Edit

---

## 🚀 Features

✅ Match user-uploaded images with IKEA product catalog  
✅ Use SSIM (Structural Similarity Index) for high-accuracy image comparison  
✅ Filter comparisons by category (e.g., "Chairs", "Tables")  
✅ Automatically fallback to standard size estimates if match confidence is low  
✅ Generate clean, printable **HTML reports**  
✅ Avoid duplicate uploads using file hash detection  
✅ Add unmatched images to dataset with a unique ID for future reference  

---

## 🛠️ Requirements

- Python 3.8+
- Install dependencies via `pip`:
  ```bash
  pip install opencv-python numpy pandas requests beautifulsoup4 scikit-image pillow
🖼️ Sample Output
After running the script, the system will:

Estimate dimensions (width, depth, height, weight)

Show matched product image (if available)

Use fallback defaults if no confident match is found

Generate a detailed HTML report

📊 Standard Fallback Dimensions
Used when image similarity confidence is below threshold.

Category	Width (cm)	Depth (cm)	Height (cm)	Weight (kg)
Chairs	45	50	90	4.5
Stool	35	35	45	2.5
Table	120	60	75	15
Cabinet	80	40	180	35
Bed	160	200	45	40

➕ Dataset Enrichment
If a product image is not matched with sufficient confidence, the tool will:

Add the image to the dataset (ikea.csv) with a new CUSTOM_<uuid> ID

Cache the image to skip future reprocessing

Use fallback dimensions based on the selected category

🤖 Integration with Spring Boot
This Python module can be plugged into your Spring Boot backend via:

REST API calls using ProcessBuilder or Runtime.exec()

File uploads handled through a temporary directory

Returning HTML reports as links or static assets

Use python/app.py as a subprocess from your Java controller to drive backend logic.

📬 Future Improvements
Train a CNN model for better image embeddings

Enrich backend functionality and add driver support

Add real-time image upload support from web clients

Deploy as a microservice using FastAPI or Flask

Detect material types using NLP on IKEA product descriptions

👨‍💻 Author
Developed by Us
Third Year Minor Project — Intelligent Packing & Moving Assistant

pgsql
Copy
Edit
