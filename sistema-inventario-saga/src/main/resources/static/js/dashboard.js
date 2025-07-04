/* Dashboard JavaScript - Saga Falabella */
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard cargado exitosamente');
    
    // Eliminar hash de la URL
    if (window.location.hash) {
        history.replaceState(null, null, window.location.pathname + window.location.search);
    }
    
    // Prevenir navegación con hash
    const hashLinks = document.querySelectorAll('a[href^="#"]');
    hashLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
        });
    });
    
    // Animaciones de entrada
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
        card.classList.add('fade-in-up');
    });
    
    // Tooltips para botones
    const tooltips = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltips.forEach(tooltip => {
        new bootstrap.Tooltip(tooltip);
    });
    
    // Manejo de errores de imágenes
    const images = document.querySelectorAll('img');
    images.forEach(img => {
        img.onerror = function() {
            this.src = '/images/placeholder.svg';
            this.alt = 'Imagen no disponible';
        };
    });
    
    // Log para debugging
    console.log('Dashboard - Elementos cargados:', {
        cards: cards.length,
        tooltips: tooltips.length,
        images: images.length
    });
});

// Funciones auxiliares
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show`;
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const container = document.querySelector('.container-fluid');
    container.insertBefore(notification, container.firstChild);
    
    setTimeout(() => {
        notification.remove();
    }, 5000);
}

function updateStats() {
    console.log('Actualizando estadísticas del dashboard...');
    // Aquí se podría hacer una llamada AJAX para actualizar las estadísticas
}

// Actualizar estadísticas cada 5 minutos
setInterval(updateStats, 300000);
