# 🧳 Packers and Movers - Intelligent Furniture Matching System

> A hybrid **Spring Boot + Python** system that intelligently matches uploaded furniture images with IKEA product data using advanced image similarity algorithms. Perfect for relocation, moving, and inventory management platforms.

## 🌟 Overview

This intelligent system uses **Structural Similarity Index (SSIM)** for precise image comparison, supports category-based filtering, and provides fallback standard dimensions for unmatched images. The tool generates comprehensive visual HTML reports with estimated product details.

### Key Highlights
- 🔍 **Smart Image Matching** - Advanced SSIM-based furniture recognition
- 📊 **Comprehensive Reports** - Auto-generated HTML reports with product details
- 🏷️ **Category Filtering** - Optimized matching within furniture categories
- 📦 **IKEA Integration** - Extensive product database with real dimensions
- 🔄 **Fallback System** - Standard estimates when matches are uncertain

---

## 📁 Project Structure

```
MigrEase/
│
├── src/
│   └── main/
│       ├── java/              # 🏗️ Spring Boot source files
│       └── resources/         # 📋 Configuration files
│
├── python/
│   ├── app.py                 # 🧠 Main Python matching engine
│   ├── ikea.csv              # 📦 IKEA product metadata database
│   ├── image_cache_ssim/     # 🖼️ Cached images for performance
│   └── report/               # 📊 Generated HTML reports
│
├── README.md                  # 📘 Project documentation
```

---

## ✨ Features

### Core Functionality
- ✅ **Image-to-Product Matching** - Match uploaded furniture images with IKEA catalog
- ✅ **SSIM Algorithm** - High-accuracy Structural Similarity Index comparison
- ✅ **Category Filtering** - Smart filtering by furniture type (Chairs, Tables, etc.)  
- ✅ **Fallback Dimensions** - Standard size estimates for low-confidence matches
- ✅ **HTML Report Generation** - Clean, printable detailed reports
- ✅ **Duplicate Detection** - File hash-based duplicate upload prevention
- ✅ **Dataset Enrichment** - Auto-add unmatched images for future reference

### Advanced Features
- 🚀 **Performance Optimized** - Image caching for faster processing
- 🎯 **Confidence Scoring** - Match quality assessment
- 📈 **Analytics Ready** - Detailed matching statistics
- 🔧 **Extensible Architecture** - Easy integration with existing systems

---

## 🛠️ Installation & Setup

### Prerequisites
- **Python 3.8+**
- **Java 11+** (for Spring Boot)
- **MySQL 8.0** (Create a database name **migrease** then only perform other stuffs.)
- **Maven** (for dependency management)

### Python Dependencies
```bash
pip install opencv-python numpy pandas requests beautifulsoup4 scikit-image pillow
```

### Quick Start
1. **Clone the repository**
   ```bash
   git clone https://github.com/siddharth-2408/MigrEase.git
   cd packers-and-movers
   ```

2. **Install Python dependencies**
   ```bash
   pip install opencv-python numpy pandas requests beautifulsoup4 scikit-image pillow
   ```

3. **Start Spring Boot application**
   ```bash
   mvn spring-boot:run
   ```

---

## 🎯 Usage Example

### Basic Image Matching
```python
# Upload furniture image and get matches
python app.py --image "chair_image.jpg" --category "Chairs"
```

### API Integration
```java
// Spring Boot controller example
@PostMapping("/match-furniture")
public ResponseEntity<String> matchFurniture(@RequestParam("file") MultipartFile file) {
    // Process image through Python subprocess
    String result = pythonMatcher.processImage(file);
    return ResponseEntity.ok(result);
}
```

---

## 📊 Standard Fallback Dimensions

When image similarity confidence is below threshold, the system uses these standard dimensions:

| Category | Width (cm) | Depth (cm) | Height (cm) | Weight (kg) |
|----------|------------|------------|-------------|-------------|
| **Chairs** | 45 | 50 | 90 | 4.5 |
| **Stools** | 35 | 35 | 45 | 2.5 |
| **Tables** | 120 | 60 | 75 | 15 |
| **Cabinets** | 80 | 40 | 180 | 35 |
| **Beds** | 160 | 200 | 45 | 40 |

---

## 🖼️ Sample Output

After processing an image, the system provides:

- 📏 **Estimated Dimensions** - Width, depth, height, and weight
- 🖼️ **Matched Product Image** - Visual confirmation of the match
- 📋 **Product Details** - IKEA product information and specifications
- 📊 **Confidence Score** - Match quality assessment
- 📄 **HTML Report** - Comprehensive formatted report

---

## 🔧 Integration with Spring Boot

### REST API Integration
The Python module integrates seamlessly with Spring Boot through:

- 🌐 **REST API Calls** - Using `ProcessBuilder` or `Runtime.exec()`
- 📁 **File Upload Handling** - Temporary directory management
- 📊 **Report Generation** - HTML reports as static assets
- 🔄 **Subprocess Communication** - Python script execution from Java controllers

### Example Controller
```java
@RestController
@RequestMapping("/api/furniture")
public class FurnitureMatchingController {
    
    @PostMapping("/match")
    public ResponseEntity<MatchResult> matchFurniture(
            @RequestParam("image") MultipartFile image,
            @RequestParam("category") String category) {
        
        // Execute Python matching script
        MatchResult result = furnitureService.processImage(image, category);
        return ResponseEntity.ok(result);
    }
}
```

---

## 🚀 Future Roadmap

### Short Term
- [ ] **CNN Model Integration** - Deep learning-based image embeddings
- [ ] **Real-time Processing** - WebSocket-based live image upload
- [ ] **Material Detection** - NLP analysis of product descriptions
- [ ] **Mobile App Support** - React Native companion app

### Long Term
- [ ] **Microservice Architecture** - FastAPI/Flask deployment
- [ ] **Cloud Integration** - AWS/GCP image processing
- [ ] **AI Enhancement** - Custom trained models for furniture recognition
- [ ] **Multi-vendor Support** - Beyond IKEA product matching

---

## 👨‍💻 Authors & Acknowledgments

**Developed by:** Teerth, Vaidehi and Siddharth 
**Project Type:** Third Year Minor Project  
**Theme:** Intelligent Packing & Moving Assistant

### Special Thanks
- 🏪 **IKEA** - For comprehensive product data
- 🐍 **OpenCV Community** - For excellent computer vision libraries
- 🌱 **Spring Boot Team** - For robust backend framework
