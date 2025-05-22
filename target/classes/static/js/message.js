document.addEventListener("DOMContentLoaded", function () {
    const flashMessage = document.getElementById("flash-message");
    if (flashMessage) {
        setTimeout(() => {
            flashMessage.style.opacity = "0";
            setTimeout(() => {
                flashMessage.style.display = "none";
            }, 500); // Wait for fade-out
        }, 3000); // Show for 3 seconds
    }
});
