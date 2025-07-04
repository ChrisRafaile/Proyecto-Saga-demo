/**
 * SISTEMA SAGA FALABELLA - JavaScript Principal
 * Funcionalidades comunes y utilidades del sistema
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */

// Configuración global
const SagaSystem = {
    // URLs base
    API_BASE: '/api',
    ASSETS_BASE: '/static',
    
    // Configuraciones
    config: {
        animationDuration: 300,
        toastDuration: 5000,
        tablePageSize: 10
    },
      // Inicializar sistema
    init() {
        this.setupEventListeners();
        this.initBootstrapComponents();
        this.initTooltips();
        this.initModals();
        this.initFormValidation();
        this.setupAjaxDefaults();
        console.log('Sistema Saga Falabella inicializado');
    },
    
    // Inicializar componentes de Bootstrap
    initBootstrapComponents() {
        // Bootstrap inicializa automáticamente los dropdowns con data-bs-toggle="dropdown"
        // No necesitamos inicialización manual para evitar conflictos
        
        // Inicializar popovers
        const popovers = document.querySelectorAll('[data-bs-toggle="popover"]');
        popovers.forEach(popover => {
            new bootstrap.Popover(popover);
        });
        
        // Inicializar collapse
        const collapses = document.querySelectorAll('.collapse');
        collapses.forEach(collapse => {
            new bootstrap.Collapse(collapse, {
                toggle: false
            });
        });
        
        console.log('Componentes Bootstrap inicializados');
    },
    
    // Configurar listeners globales
    setupEventListeners() {
        // Confirmar eliminaciones
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-delete') || e.target.closest('.btn-delete')) {
                e.preventDefault();
                this.confirmDelete(e.target.closest('.btn-delete'));
            }
        });
        
        // Auto-submit en selects marcados
        document.addEventListener('change', (e) => {
            if (e.target.classList.contains('auto-submit')) {
                e.target.closest('form').submit();
            }
        });
        
        // Loading en formularios
        document.addEventListener('submit', (e) => {
            const form = e.target;
            if (form.tagName === 'FORM' && !form.classList.contains('no-loading')) {
                this.showFormLoading(form);
            }
        });
    },
    
    // Inicializar tooltips de Bootstrap
    initTooltips() {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    },
    
    // Inicializar modales
    initModals() {
        // Auto-focus en inputs de modales
        document.addEventListener('shown.bs.modal', (e) => {
            const firstInput = e.target.querySelector('input, select, textarea');
            if (firstInput && !firstInput.disabled) {
                firstInput.focus();
            }
        });
    },
    
    // Validación de formularios
    initFormValidation() {
        // Validación personalizada de confirmación de contraseña
        const passwordFields = document.querySelectorAll('input[type="password"][id*="confirm"]');
        passwordFields.forEach(field => {
            field.addEventListener('input', this.validatePasswordConfirm);
        });
        
        // Validación de DNI peruano
        const dniFields = document.querySelectorAll('input[name="dni"], input[id="dni"]');
        dniFields.forEach(field => {
            field.addEventListener('input', this.validateDNI);
        });
        
        // Validación de teléfono
        const phoneFields = document.querySelectorAll('input[type="tel"]');
        phoneFields.forEach(field => {
            field.addEventListener('input', this.validatePhone);
        });
    },
    
    // Configurar AJAX por defecto
    setupAjaxDefaults() {
        // Agregar token CSRF a todas las peticiones AJAX
        const token = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');
        
        if (token && header) {
            $.ajaxSetup({
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(header.getAttribute('content'), token.getAttribute('content'));
                }
            });
        }
    },
    
    // Utilidades
    utils: {
        // Formatear moneda
        formatCurrency(amount) {
            return new Intl.NumberFormat('es-PE', {
                style: 'currency',
                currency: 'PEN'
            }).format(amount);
        },
        
        // Formatear fecha
        formatDate(date) {
            return new Intl.DateTimeFormat('es-PE', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit'
            }).format(new Date(date));
        },
        
        // Formatear fecha y hora
        formatDateTime(date) {
            return new Intl.DateTimeFormat('es-PE', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            }).format(new Date(date));
        },
        
        // Generar ID único
        generateId() {
            return '_' + Math.random().toString(36).substr(2, 9);
        },
        
        // Capitalizar primera letra
        capitalize(str) {
            return str.charAt(0).toUpperCase() + str.slice(1);
        },
        
        // Debounce function
        debounce(func, wait) {
            let timeout;
            return function executedFunction(...args) {
                const later = () => {
                    clearTimeout(timeout);
                    func(...args);
                };
                clearTimeout(timeout);
                timeout = setTimeout(later, wait);
            };
        }
    },
    
    // Mostrar toast/notificación
    showToast(message, type = 'info', duration = null) {
        duration = duration || this.config.toastDuration;
        
        const toastContainer = document.getElementById('toast-container') || this.createToastContainer();
        const toastId = this.utils.generateId();
        
        const toastHTML = `
            <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert">
                <div class="d-flex">
                    <div class="toast-body">
                        <i class="fas fa-${this.getToastIcon(type)} me-2"></i>
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;
        
        toastContainer.insertAdjacentHTML('beforeend', toastHTML);
        const toastElement = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastElement, { delay: duration });
        toast.show();
        
        // Remover del DOM después de que se oculte
        toastElement.addEventListener('hidden.bs.toast', () => {
            toastElement.remove();
        });
    },
    
    // Crear contenedor de toasts
    createToastContainer() {
        const container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container position-fixed top-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
        return container;
    },
    
    // Obtener icono para toast
    getToastIcon(type) {
        const icons = {
            'success': 'check-circle',
            'danger': 'exclamation-circle',
            'warning': 'exclamation-triangle',
            'info': 'info-circle',
            'primary': 'info-circle'
        };
        return icons[type] || 'info-circle';
    },
    
    // Confirmar eliminación
    confirmDelete(element) {
        const itemName = element.dataset.itemName || 'este elemento';
        const message = `¿Estás seguro de que deseas eliminar ${itemName}? Esta acción no se puede deshacer.`;
        
        if (confirm(message)) {
            // Si el elemento es un enlace, navegar a la URL
            if (element.tagName === 'A') {
                window.location.href = element.href;
            }
            // Si está dentro de un formulario, enviarlo
            else if (element.form) {
                element.form.submit();
            }
            // Si tiene data-url, hacer petición AJAX
            else if (element.dataset.url) {
                this.deleteItem(element.dataset.url, element);
            }
        }
    },
    
    // Eliminar elemento vía AJAX
    deleteItem(url, element) {
        const row = element.closest('tr');
        
        fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            if (response.ok) {
                this.showToast('Elemento eliminado exitosamente', 'success');
                if (row) {
                    row.style.transition = 'opacity 0.3s';
                    row.style.opacity = '0';
                    setTimeout(() => row.remove(), 300);
                } else {
                    location.reload();
                }
            } else {
                throw new Error('Error al eliminar');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            this.showToast('Error al eliminar el elemento', 'danger');
        });
    },
    
    // Mostrar loading en formulario
    showFormLoading(form) {
        const submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Procesando...';
            
            // Restaurar después de 10 segundos como failsafe
            setTimeout(() => {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }, 10000);
        }
    },
    
    // Validación de confirmación de contraseña
    validatePasswordConfirm(event) {
        const confirmField = event.target;
        const passwordField = document.querySelector('input[type="password"]:not([id*="confirm"])');
        
        if (passwordField && confirmField.value !== passwordField.value) {
            confirmField.setCustomValidity('Las contraseñas no coinciden');
        } else {
            confirmField.setCustomValidity('');
        }
    },
    
    // Validación de DNI
    validateDNI(event) {
        const field = event.target;
        const dni = field.value.replace(/\D/g, ''); // Solo números
        
        field.value = dni;
        
        if (dni.length === 8) {
            // Validación básica de DNI peruano
            if (!/^[0-9]{8}$/.test(dni)) {
                field.setCustomValidity('DNI debe tener 8 dígitos');
            } else {
                field.setCustomValidity('');
            }
        } else if (dni.length > 0) {
            field.setCustomValidity('DNI debe tener 8 dígitos');
        } else {
            field.setCustomValidity('');
        }
    },
    
    // Validación de teléfono
    validatePhone(event) {
        const field = event.target;
        const phone = field.value.replace(/\D/g, ''); // Solo números
        
        field.value = phone;
        
        if (phone.length === 9) {
            // Validación básica de teléfono peruano
            if (!/^9[0-9]{8}$/.test(phone)) {
                field.setCustomValidity('Teléfono debe empezar con 9 y tener 9 dígitos');
            } else {
                field.setCustomValidity('');
            }
        } else if (phone.length > 0) {
            field.setCustomValidity('Teléfono debe tener 9 dígitos');
        } else {
            field.setCustomValidity('');
        }
    },
    
    // Mostrar modal de carga
    showLoadingModal(message = 'Procesando...') {
        const modalHTML = `
            <div class="modal fade" id="loadingModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
                <div class="modal-dialog modal-sm modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-body text-center p-4">
                            <div class="spinner-saga mb-3 mx-auto"></div>
                            <p class="mb-0">${message}</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        const modal = new bootstrap.Modal(document.getElementById('loadingModal'));
        modal.show();
        
        return modal;
    },
    
    // Ocultar modal de carga
    hideLoadingModal() {
        const modal = bootstrap.Modal.getInstance(document.getElementById('loadingModal'));
        if (modal) {
            modal.hide();
            setTimeout(() => {
                document.getElementById('loadingModal')?.remove();
            }, 300);
        }
    }
};

// Funciones específicas para diferentes módulos
const ProductoModule = {
    // Buscar productos
    searchProducts: SagaSystem.utils.debounce(function(query) {
        if (query.length < 2) return;
        
        fetch(`${SagaSystem.API_BASE}/productos/search?q=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(data => {
                // Actualizar resultados de búsqueda
                const resultsContainer = document.getElementById('search-results');
                if (resultsContainer) {
                    resultsContainer.innerHTML = data.map(producto => `
                        <div class="search-result-item p-2 border-bottom">
                            <h6>${producto.nombre}</h6>
                            <p class="text-muted small">${producto.descripcion}</p>
                            <span class="text-primary fw-bold">${SagaSystem.utils.formatCurrency(producto.precio)}</span>
                        </div>
                    `).join('');
                }
            })
            .catch(error => {
                console.error('Error en búsqueda:', error);
            });
    }, 300),
    
    // Agregar al carrito
    addToCart(productId, quantity = 1) {
        const loadingModal = SagaSystem.showLoadingModal('Agregando al carrito...');
        
        fetch(`${SagaSystem.API_BASE}/carrito/agregar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ productId, quantity })
        })
        .then(response => response.json())
        .then(data => {
            SagaSystem.hideLoadingModal();
            if (data.success) {
                SagaSystem.showToast('Producto agregado al carrito', 'success');
                // Actualizar contador del carrito
                this.updateCartCount();
            } else {
                SagaSystem.showToast(data.message || 'Error al agregar al carrito', 'danger');
            }
        })
        .catch(error => {
            SagaSystem.hideLoadingModal();
            console.error('Error:', error);
            SagaSystem.showToast('Error al agregar al carrito', 'danger');
        });
    },
    
    // Actualizar contador del carrito
    updateCartCount() {
        fetch(`${SagaSystem.API_BASE}/carrito/count`)
            .then(response => response.json())
            .then(data => {
                const cartBadge = document.querySelector('.cart-count');
                if (cartBadge) {
                    cartBadge.textContent = data.count;
                    cartBadge.style.display = data.count > 0 ? 'inline' : 'none';
                }
            })
            .catch(error => console.error('Error:', error));
    }
};

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    SagaSystem.init();
    
    // Configurar búsqueda de productos si existe el campo
    const searchInput = document.getElementById('product-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            ProductoModule.searchProducts(e.target.value);
        });
    }
    
    // Configurar botones de agregar al carrito
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('btn-add-cart')) {
            e.preventDefault();
            const productId = e.target.dataset.productId;
            const quantity = e.target.dataset.quantity || 1;
            ProductoModule.addToCart(productId, quantity);
        }
    });
});

// Exportar para uso global
window.SagaSystem = SagaSystem;
window.ProductoModule = ProductoModule;
