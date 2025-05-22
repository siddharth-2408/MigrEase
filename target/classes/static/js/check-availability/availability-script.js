function debounce(func, delay) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), delay);
    };
}

async function fetchLocation(pincode, elementId) {
    const locationElement = document.getElementById(elementId);

    if (!pincode || pincode.length !== 6) {
        locationElement.innerText = pincode.length > 0 ? "⚠️ Enter a valid 6-digit Pincode!" : "";
        locationElement.className = "location-text error";
        return null;
    }

    locationElement.innerText = "🔍 Searching...";
    locationElement.className = "location-text loading";

    try {
        const response = await fetch(`https://api.postalpincode.in/pincode/${pincode}`);
        const data = await response.json();

        if (
            data &&
            data[0]?.Status === "Success" &&
            Array.isArray(data[0].PostOffice) &&
            data[0].PostOffice.length > 0
        ) {
            const location = data[0].PostOffice[0];
            const locationString = `${location.Name}, ${location.District}, ${location.State}`;
            locationElement.innerText = `📍 ${locationString}`;
            locationElement.className = "location-text success";
            return locationString;
        } else {
            locationElement.innerText = "⚠️ Location not found!";
            locationElement.className = "location-text error";
            return null;
        }
    } catch (error) {
        console.error("Error fetching location:", error);
        locationElement.innerText = "⚠️ Error fetching location!";
        locationElement.className = "location-text error";
        return null;
    }
}

async function checkAvailability() {
    const pickupPincode = document.getElementById("pickupPincode").value.trim();
    const dropPincode = document.getElementById("dropPincode").value.trim();
    const resultElement = document.getElementById("result");

    resultElement.innerText = "";
    resultElement.style.color = "#ff8c00"; // orange for loading

    if (pickupPincode.length !== 6 || dropPincode.length !== 6) {
        resultElement.innerText = "⚠️ Please enter valid 6-digit pin codes!";
        resultElement.style.color = "#e74c3c";
        return;
    }

    resultElement.innerText = "Checking service availability...";

    const pickupLocation = await fetchLocation(pickupPincode, "pickupLocation");
    const dropLocation = await fetchLocation(dropPincode, "dropLocation");

    if (!pickupLocation || !dropLocation) {
        resultElement.innerText = "❌ Invalid pincodes! Could not fetch both locations.";
        resultElement.style.color = "#e74c3c";
        return;
    }

    // Your own logic: If both are fetched, you consider service available.
    resultElement.innerText = `✅ Service available from "${pickupLocation}" to "${dropLocation}" 🚚`;
    resultElement.style.color = "#2ecc71"; // green
}

// Debounced input fetch
const debouncedFetchLocation = debounce((value, elementId) => {
    fetchLocation(value, elementId);
}, 300);

// Input listeners
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById("pickupPincode").addEventListener("input", function () {
        debouncedFetchLocation(this.value.trim(), "pickupLocation");
    });

    document.getElementById("dropPincode").addEventListener("input", function () {
        debouncedFetchLocation(this.value.trim(), "dropLocation");
    });
});
