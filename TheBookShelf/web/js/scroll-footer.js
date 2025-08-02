document.addEventListener('DOMContentLoaded', function () {
    let lastScrollTop = 0;
    const footer = document.querySelector('footer'); 

    if (footer) {
        window.addEventListener('scroll', function () {
            const currentScroll = window.pageYOffset || document.documentElement.scrollTop;

            if (currentScroll > lastScrollTop) {
                footer.classList.add('hidden-footer');
            } else {
                footer.classList.remove('hidden-footer');
            }

            lastScrollTop = currentScroll <= 0 ? 0 : currentScroll;
        });
    }
});
