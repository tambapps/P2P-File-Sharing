// main.js
document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("learnMoreBtn");
    const section = document.getElementById("about");

    if (btn && section) {
        btn.addEventListener("click", () => {
            section.scrollIntoView({ behavior: "smooth" });
        });
    }
});
