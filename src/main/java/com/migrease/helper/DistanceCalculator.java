package com.migrease.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class DistanceCalculator {



    public double[] getLatLng(String address, String apiKey) throws IOException, InterruptedException {
        String url = "https://us1.locationiq.com/v1/search.php?key=" + apiKey +
                "&q=" + URLEncoder.encode(address, StandardCharsets.UTF_8) + "&format=json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        if (root.isArray() && root.size() > 0) {
            JsonNode node = root.get(0);

            // ✅ Use asText() and parse explicitly
            double lat = Double.parseDouble(node.get("lat").asText());
            double lon = Double.parseDouble(node.get("lon").asText());

            System.out.println("✅ Found coordinates for: " + address);
            System.out.println("   ➤ Lat: " + lat + ", Lon: " + lon);

            return new double[]{lat, lon};
        } else {
            System.out.println("❌ Failed to find coordinates for: " + address);
            throw new RuntimeException("No results from LocationIQ for address: " + address);
        }
    }

    public double getDrivingDistance(double startLat, double startLon, double endLat, double endLon) throws IOException, InterruptedException {
        String apiKey = "5b3ce3597851110001cf62484b1c6b8ef4d04479aaffbd18f10f916e";

        String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + apiKey;

        String jsonPayload = """
        {
          "coordinates": [[%f, %f], [%f, %f]]
        }
        """.formatted(startLon, startLat, endLon, endLat);  // Lon, Lat format

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("🌐 Raw API response: " + response.body());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        double distanceInMeters = root.at("/routes/0/summary/distance").asDouble();
        double distanceInKm = distanceInMeters / 1000.0;

        System.out.println("🚛 Actual driving distance: " + distanceInKm + " km");
        return distanceInKm;
    }
}
