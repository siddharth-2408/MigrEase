package com.migrease.controllers;

import com.migrease.model.FurnitureItem;
import com.migrease.model.User;
import com.migrease.repository.FurnitureItemRepository;
import com.migrease.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.regex.*;

@RestController
@RequestMapping("/api/furniture")
public class FurnitureRecognitionController {

    private static final String PYTHON_SCRIPT_PATH;
    private static final String REPORT_HTML_PATH;

    static {
        Path root = Paths.get("").toAbsolutePath();
        PYTHON_SCRIPT_PATH = root.resolve("python/app.py").toString();
        REPORT_HTML_PATH = root.resolve("python/report/report.html").toString();
    }

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private FurnitureItemRepository furnitureItemRepository;

    // ✅ Densities in kg/m³
    private static final Map<String, Map<String, Double>> densityMap = Map.of(
            "Beds", Map.of(
                    "Solid Wood", 750.0,
                    "Plywood", 550.0,
                    "MDF", 650.0
            ),
            "Chairs", Map.of(
                    "Plastic", 200.0,
                    "Metal", 780.0,
                    "Wood", 700.0
            ),
            "Sofas & armchairs", Map.of(
                    "Leather", 600.0,
                    "Fabric", 450.0,
                    "Wood", 700.0
            ),
            "Tables & desks", Map.of(
                    "Wood", 720.0,
                    "Glass", 500.0,
                    "Metal", 850.0
            ),
            "Cabinets & cupboards", Map.of(
                    "Plywood", 560.0,
                    "Laminated Wood", 600.0,
                    "Metal", 820.0
            ),
            "TV & media furniture", Map.of(
                    "Engineered Wood", 580.0,
                    "Glass", 500.0,
                    "MDF", 620.0
            ),
            "Wardrobes", Map.of(
                    "Plywood", 550.0,
                    "Particle Board", 520.0,
                    "Solid Wood", 750.0
            )
    );

    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeFurniture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("quality") String quality,
            @RequestParam("quantity") int quantity,
            Principal principal
    ) {

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("furniture-", ".png");
            file.transferTo(tempFile.toFile());

            List<String> command = new ArrayList<>();
            command.add("python");
            command.add(PYTHON_SCRIPT_PATH);
            command.add("--input_path=" + tempFile.toAbsolutePath());
            if (category != null && !category.isBlank()) {
                command.add("--category=" + category);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (reader.readLine() != null); // discard script output

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Python script failed.");
            }

            Path reportPath = Paths.get(REPORT_HTML_PATH);
            if (!Files.exists(reportPath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Report not found.");
            }

            String reportContent = Files.readString(reportPath);

            // Extract original dimensions in cm
            double width = extractValue(reportContent, "Width");
            double depth = extractValue(reportContent, "Depth");
            double height = extractValue(reportContent, "Height");

            // Volume in m³
            double volume = (width * height * depth) / 1_000_000.0;

            // Get density (kg/m³)
            double density = densityMap
                    .getOrDefault(category, Map.of())
                    .getOrDefault(quality, 600.0);

            // Apply scaling factor to keep realistic weights
            double weight = volume * density * quantity * 0.05;

            // Replace original weight in report with computed one
            String updatedReport = reportContent.replaceAll(
                    "(<th>Weight.*?</th><td>)([\\d.]+)(\\s*kg</td>)",
                    "$1" + String.format("%.2f", weight) + "$3"
            );

            // Save to DB
            Optional<User> userOpt = userRepository.findByEmail(principal.getName());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            FurnitureItem item = new FurnitureItem();
            item.setCategory(category);
            item.setWidth(width);
            item.setHeight(height);
            item.setDepth(depth);
            item.setWeight(weight);
            item.setQuantity(quantity);
            item.setQuality(quality);
            item.setUser(userOpt.get());

            furnitureItemRepository.save(item);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "text/html");
            return new ResponseEntity<>(updatedReport, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing image: " + e.getMessage());
        } finally {
            try {
                if (tempFile != null && Files.exists(tempFile)) {
                    Files.delete(tempFile);
                }
            } catch (Exception ignored) {}
        }
    }

    private double extractValue(String html, String label) {
        try {
            String regex = "<th>" + label + "</th><td>([\\d.]+)";
            Matcher matcher = Pattern.compile(regex).matcher(html);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (Exception ignored) {}
        return 0.0;
    }
}
