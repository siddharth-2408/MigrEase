const categories = ['Beds', 'Cabinets & cupboards', 'Chairs', 'Sofas & armchairs', 'Tables & desks', 'TV & media furniture', 'Wardrobes'];

const materialsByCategory = {
    'Beds': ['Solid Wood', 'Plywood', 'MDF'],
    'Chairs': ['Plastic', 'Metal', 'Wood'],
    'Sofas & armchairs': ['Leather', 'Fabric', 'Wood'],
    'Tables & desks': ['Wood', 'Glass', 'Metal'],
    'Cabinets & cupboards': ['Plywood', 'Laminated Wood', 'Metal'],
    'TV & media furniture': ['Engineered Wood', 'Glass', 'MDF'],
    'Wardrobes': ['Plywood', 'Particle Board', 'Solid Wood']
};

let currentCategory = 0;

// DOM ready handler
document.addEventListener('DOMContentLoaded', function() {
    // Set up drag and drop for file upload
    const dropArea = document.getElementById('dropArea');
    const fileInput = document.getElementById('categoryImage');

    if (dropArea) {
        // Prevent default drag behaviors
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropArea.addEventListener(eventName, preventDefaults, false);
        });

        // Highlight drop area when item is dragged over it
        ['dragenter', 'dragover'].forEach(eventName => {
            dropArea.addEventListener(eventName, highlight, false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropArea.addEventListener(eventName, unhighlight, false);
        });

        // Handle dropped files
        dropArea.addEventListener('drop', handleDrop, false);

        // Handle click to upload
        dropArea.addEventListener('click', function() {
            fileInput.click();
        });
    }

    // Handle file input change
    if (fileInput) {
        fileInput.addEventListener('change', handleFileSelect);
    }

    // Set up quantity buttons
    const decreaseBtn = document.getElementById('decreaseQty');
    const increaseBtn = document.getElementById('increaseQty');
    const quantityInput = document.getElementById('quantityInput');

    if (decreaseBtn && increaseBtn && quantityInput) {
        decreaseBtn.addEventListener('click', function() {
            let value = parseInt(quantityInput.value);
            if (value > 1) {
                quantityInput.value = value - 1;
            }
        });

        increaseBtn.addEventListener('click', function() {
            let value = parseInt(quantityInput.value);
            quantityInput.value = value + 1;
        });
    }

    // Handle remove image button
    const removeImageBtn = document.getElementById('removeImage');
    if (removeImageBtn) {
        removeImageBtn.addEventListener('click', function(e) {
            e.stopPropagation(); // Prevent triggering click on the drop area
            clearFileInput();
        });
    }
});

function startCategoryFlow() {
    const pickup = {
        house: document.getElementById("pickupHouse").value.trim(),
        street: document.getElementById("pickupStreet").value.trim(),
        city: document.getElementById("pickupCity").value.trim(),
        pincode: document.getElementById("pickupPincode").value.trim()
    };
    const drop = {
        house: document.getElementById("dropHouse").value.trim(),
        street: document.getElementById("dropStreet").value.trim(),
        city: document.getElementById("dropCity").value.trim(),
        pincode: document.getElementById("dropPincode").value.trim()
    };

    if (!pickup.house || !pickup.street || !pickup.city || !pickup.pincode ||
        !drop.house || !drop.street || !drop.city || !drop.pincode) {
        showNotification("Please fill all pickup and drop address fields.", "error");
        return;
    }

    const payload = {
        pickupHouse: pickup.house,
        pickupStreet: pickup.street,
        pickupCity: pickup.city,
        pickupPincode: pickup.pincode,
        dropHouse: drop.house,
        dropStreet: drop.street,
        dropCity: drop.city,
        dropPincode: drop.pincode
    };

    // Show loading state
    const btnNext = document.querySelector('.container > .btn');
    if (btnNext) {
        btnNext.disabled = true;
        btnNext.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
    }

    fetch("/api/address/save", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(res => {
            // Check if it's a redirect response (HTTP 302 Found)
            if (res.status === 302) {
                return res.json().then(data => {
                    if (data.redirect && data.message) {
                        showNotification(data.message, "info");
                        setTimeout(() => {
                            window.location.href = data.redirect;
                        }, 2000);
                        return { redirected: true };
                    }
                    return data;
                });
            }
            return res.json();
        })
        .then(data => {
            // Skip further processing if redirected
            if (data.redirected) return;

            // Continue with normal flow
            sessionStorage.setItem("addressId", data.addressId);
            document.getElementById("categorySection").classList.remove("hidden");
            updateCategoryUI();

            // Scroll to category section
            document.getElementById("categorySection").scrollIntoView({ behavior: 'smooth' });
        })
        .catch(err => {
            console.error("Failed to save address:", err);
            showNotification("Address could not be saved.", "error");
        })
        .finally(() => {
            if (btnNext && !document.getElementById("categorySection").classList.contains("hidden")) {
                btnNext.disabled = false;
                btnNext.innerHTML = '<i class="fas fa-arrow-right"></i> Next: Add Items';
            }
        });
}

function autoFillCity(type) {
    const pincode = document.getElementById(`${type}Pincode`).value.trim();
    if (pincode.length === 6) {
        // Show loading indicator inside the city input
        const cityInput = document.getElementById(`${type}City`);
        cityInput.value = "Loading...";

        fetch(`https://api.postalpincode.in/pincode/${pincode}`)
            .then(res => res.json())
            .then(data => {
                const city = data[0]?.PostOffice?.[0]?.District;
                if (city) {
                    cityInput.value = city;
                } else {
                    cityInput.value = "";
                    showNotification("Invalid pincode.", "error");
                }
            })
            .catch(() => {
                cityInput.value = "";
                showNotification("Failed to fetch city.", "error");
            });
    }
}

function updateCategoryUI() {
    const category = categories[currentCategory];
    document.getElementById("categoryName").innerText = category;
    document.getElementById("categoryHint").innerText = category.toLowerCase();
    document.getElementById("stepCount").innerText = currentCategory + 1;

    // Clear any existing file
    clearFileInput();

    // Update quantity
    document.getElementById("quantityInput").value = 1;

    // Update progress bar
    const progressPercentage = ((currentCategory + 1) / categories.length) * 100;
    document.getElementById("progressFill").style.width = `${progressPercentage}%`;

    // Update quality/material select
    const qualitySelect = document.getElementById("qualitySelect");
    qualitySelect.innerHTML = '<option value="">Select material</option>';
    (materialsByCategory[category] || []).forEach(q => {
        const opt = document.createElement("option");
        opt.value = q;
        opt.text = q;
        qualitySelect.appendChild(opt);
    });
}

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}

function highlight() {
    document.getElementById('dropArea').classList.add('dragover');
}

function unhighlight() {
    document.getElementById('dropArea').classList.remove('dragover');
}

function handleDrop(e) {
    const dt = e.dataTransfer;
    const files = dt.files;

    if (files.length) {
        document.getElementById('categoryImage').files = files;
        handleFilePreview(files[0]);
    }
}

function handleFileSelect(e) {
    if (e.target.files.length) {
        handleFilePreview(e.target.files[0]);
    }
}

function handleFilePreview(file) {
    // Validate file is an image
    if (!file.type.match('image.*')) {
        showNotification("Please select an image file.", "error");
        clearFileInput();
        return;
    }

    // Show preview
    const reader = new FileReader();
    reader.onload = function(e) {
        document.getElementById('imagePreview').src = e.target.result;
        document.getElementById('previewContainer').classList.remove('hidden');
        document.getElementById('dropArea').classList.add('has-preview');

        // Hide the upload icon and text
        document.querySelector('.upload-icon').style.display = 'none';
        document.querySelector('.upload-text').style.display = 'none';
        document.querySelector('.upload-hint').style.display = 'none';
    }
    reader.readAsDataURL(file);
}

function clearFileInput() {
    const fileInput = document.getElementById('categoryImage');
    fileInput.value = '';

    // Reset the preview
    document.getElementById('previewContainer').classList.add('hidden');
    document.getElementById('dropArea').classList.remove('has-preview');

    // Show the upload icon and text again
    document.querySelector('.upload-icon').style.display = 'block';
    document.querySelector('.upload-text').style.display = 'block';
    document.querySelector('.upload-hint').style.display = 'block';
}

function uploadImageAndNext() {
    const fileInput = document.getElementById("categoryImage");
    const quality = document.getElementById("qualitySelect").value;
    const quantity = parseInt(document.getElementById("quantityInput").value);

    if (!fileInput.files[0]) {
        showNotification("Please select an image.", "error");
        return;
    }
    if (!quality) {
        showNotification("Please select material quality.", "error");
        return;
    }
    if (isNaN(quantity) || quantity <= 0) {
        showNotification("Please enter a valid quantity.", "error");
        return;
    }

    // Show loading with text
    const uploadLoader = document.getElementById("uploadLoader");
    uploadLoader.classList.remove("hidden");
    uploadLoader.innerHTML = '<div class="spinner"></div><p class="loader-text">Uploading your ' + categories[currentCategory].toLowerCase() + '...</p>';

    document.getElementById("uploadBtn").disabled = true;

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("category", categories[currentCategory]);
    formData.append("quality", quality);
    formData.append("quantity", quantity);
    formData.append("addressId", sessionStorage.getItem("addressId"));

    fetch("/api/furniture/recognize", {
        method: "POST",
        body: formData
    })
        .then(res => res.text())
        .then(html => {
            const blob = new Blob([html], { type: "text/html" });
            const url = URL.createObjectURL(blob);
            const frame = document.getElementById("reportFrame");
            frame.src = url;
            frame.classList.remove("hidden");

            currentCategory++;
            if (currentCategory < categories.length) {
                updateCategoryUI();
            } else {
                showLoaderAndRedirect();
            }
        })
        .catch(() => showNotification("Failed to upload image.", "error"))
        .finally(() => {
            uploadLoader.classList.add("hidden");
            document.getElementById("uploadBtn").disabled = false;
        });
}

function skipCategory() {
    currentCategory++;
    if (currentCategory < categories.length) {
        updateCategoryUI();
    } else {
        showLoaderAndRedirect();
    }
}

function showLoaderAndRedirect() {
    document.getElementById("categorySection").classList.add("hidden");
    document.getElementById("reportFrame").classList.add("hidden");

    const redirectLoader = document.getElementById("redirectLoader");
    redirectLoader.style.display = "flex";
    redirectLoader.innerHTML = '<div class="spinner"></div><p class="loader-text">Preparing your booking summary...</p>';

    setTimeout(() => {
        window.location.href = "/user/confirm-booking";
    }, 2000);
}

// Helper function to show notifications
function showNotification(message, type = 'info') {
    // Check if notification container exists, if not create it
    let notifContainer = document.getElementById('notification-container');
    if (!notifContainer) {
        notifContainer = document.createElement('div');
        notifContainer.id = 'notification-container';
        notifContainer.style.position = 'fixed';
        notifContainer.style.top = '20px';
        notifContainer.style.right = '20px';
        notifContainer.style.zIndex = '10000';
        document.body.appendChild(notifContainer);
    }

    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.style.backgroundColor = type === 'error' ? '#ff4c4c' : '#4CAF50';
    notification.style.color = 'white';
    notification.style.padding = '12px 20px';
    notification.style.marginBottom = '10px';
    notification.style.borderRadius = '5px';
    notification.style.boxShadow = '0 3px 10px rgba(0, 0, 0, 0.15)';
    notification.style.display = 'flex';
    notification.style.alignItems = 'center';
    notification.style.justifyContent = 'space-between';
    notification.style.minWidth = '280px';
    notification.style.animation = 'slideIn 0.3s ease-out forwards';

    // Add icon based on type
    const icon = document.createElement('i');
    icon.className = type === 'error' ? 'fas fa-exclamation-circle' : 'fas fa-check-circle';
    icon.style.marginRight = '10px';

    const textSpan = document.createElement('span');
    textSpan.textContent = message;

    const closeBtn = document.createElement('i');
    closeBtn.className = 'fas fa-times';
    closeBtn.style.marginLeft = '10px';
    closeBtn.style.cursor = 'pointer';
    closeBtn.style.opacity = '0.7';
    closeBtn.style.transition = 'opacity 0.2s';
    closeBtn.addEventListener('mouseenter', () => {
        closeBtn.style.opacity = '1';
    });
    closeBtn.addEventListener('mouseleave', () => {
        closeBtn.style.opacity = '0.7';
    });
    closeBtn.addEventListener('click', () => {
        notification.style.animation = 'slideOut 0.3s ease-in forwards';
        setTimeout(() => {
            notification.remove();
        }, 300);
    });

    // Add elements to notification
    notification.appendChild(icon);
    notification.appendChild(textSpan);
    notification.appendChild(closeBtn);

    // Add animation styles
    const styleEl = document.createElement('style');
    styleEl.innerHTML = `
      @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
      }
      @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
      }
    `;
    document.head.appendChild(styleEl);

    // Add notification to container
    notifContainer.appendChild(notification);

    // Auto remove after delay
    setTimeout(() => {
        if (notification) {
            notification.style.animation = 'slideOut 0.3s ease-in forwards';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }
    }, 5000);
}