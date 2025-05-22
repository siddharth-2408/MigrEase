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
- pip packages (install via `requirements.txt` or manually):
  ```bash
  pip install opencv-python numpy pandas requests beautifulsoup4 scikit-image pillow

🖼️ Sample Output
After running the script, it will:

Estimated dimensions (width, depth, height, weight)

Product image (if matched)

Fallback defaults if no confident match is found

📊 Standard Fallback Dimensions
When no close image match is found, the system uses the following estimates:

Category	Width (cm)	Depth (cm)	Height (cm)	Weight (kg)
Chairs	45	50	90	4.5
Stool	35	35	45	2.5
Table	120	60	75	15
Cabinet	80	40	180	35
Bed	160	200	45	40

➕ Dataset Enrichment
If a product image is not matched with sufficient confidence, the tool:

Adds the image to the dataset (ikea.csv) with a new CUSTOM_<uuid> ID

Caches the image to avoid future reprocessing

Uses fallback dimensions based on category

🤖 Integration with Spring Boot
This Python module can be integrated into a Spring Boot backend using:

REST API calls via ProcessBuilder or Runtime.exec()

File uploads sent to a temp directory

HTML reports returned as links or static files

Use python/app.py as a subprocess within your Java controller to power backend logic.

📬 Future Improvements
Train a CNN model for better image embeddings

Still we have to enrich our backend and add a Driver Support to it (Just built for the User's Perspective)

Add support for real-time web image uploads

Deploy as a microservice via FastAPI or Flask

Integrate material detection using NLP on IKEA pages

👨‍💻 Author
Developed by Us
Third Year Minor Project — Intelligent Packing & Moving Assistant
