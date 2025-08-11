document.addEventListener("DOMContentLoaded", () => {
  const carousel = document.querySelector(".custom-carousel");
  const slides = carousel.querySelectorAll(".carousel-slide");
  const indicators = carousel.querySelectorAll(".indicator");
  const prevBtn = carousel.querySelector(".carousel-control.prev");
  const nextBtn = carousel.querySelector(".carousel-control.next");

  let currentIndex = 0;
  const totalSlides = slides.length;

  function updateCarousel() {
    // Move slides container
    const slidesContainer = carousel.querySelector(".carousel-slides");
    slidesContainer.style.transform = `translateX(-${currentIndex * 100}%)`;

    // Update active slide opacity
    slides.forEach((slide, idx) => {
      slide.classList.toggle("active", idx === currentIndex);
    });

    // Update indicators
    indicators.forEach((indicator, idx) => {
      indicator.classList.toggle("active", idx === currentIndex);
    });
  }

  // Indicator click event
  indicators.forEach((indicator, idx) => {
    indicator.addEventListener("click", () => {
      currentIndex = idx;
      updateCarousel();
    });
  });

  // Prev/Next buttons
  prevBtn.addEventListener("click", () => {
    currentIndex = (currentIndex - 1 + totalSlides) % totalSlides;
    updateCarousel();
  });

  nextBtn.addEventListener("click", () => {
    currentIndex = (currentIndex + 1) % totalSlides;
    updateCarousel();
  });

  // Auto-slide every 5 seconds (optional)
  setInterval(() => {
    currentIndex = (currentIndex + 1) % totalSlides;
    updateCarousel();
  }, 5000);

  // Initialize carousel on load
  updateCarousel();
});
