document.addEventListener('DOMContentLoaded', () => {
    const slides = document.querySelectorAll('.carousel-slide');
    const indicators = document.querySelectorAll('.carousel-indicators .indicator');
    const prevButton = document.querySelector('.carousel-control.prev');
    const nextButton = document.querySelector('.carousel-control.next');
    let currentIndex = 0;

    function showSlide(index) {
        slides.forEach((slide, i) => {
            slide.classList.remove('active');
            indicators[i].classList.remove('active');
            if (i === index) {
                slide.classList.add('active');
                indicators[i].classList.add('active');
            }
        });
    }

    function nextSlide() {
        currentIndex = (currentIndex + 1) % slides.length;
        showSlide(currentIndex);
    }

    function prevSlide() {
        currentIndex = (currentIndex - 1 + slides.length) % slides.length;
        showSlide(currentIndex);
    }

    indicators.forEach((indicator, i) => {
        indicator.addEventListener('click', () => {
            currentIndex = i;
            showSlide(currentIndex);
        });
    });

    prevButton.addEventListener('click', prevSlide);
    nextButton.addEventListener('click', nextSlide);

    // Auto-slide every 5 seconds
    setInterval(nextSlide, 5000);

    // Show initial slide
    showSlide(currentIndex);
});