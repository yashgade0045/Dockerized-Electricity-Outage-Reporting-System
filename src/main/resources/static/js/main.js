/**
 * Electricity Outage Reporting System — Main JavaScript
 * Handles: delete confirmations, auto-dismiss alerts, search UX, form feedback
 */

'use strict';

/**
 * Prompt the user before performing a destructive delete operation.
 * Called via onsubmit on the delete form in the detail page.
 * @returns {boolean} true to proceed, false to cancel
 */
function confirmDelete() {
    return window.confirm(
        'Are you sure you want to permanently delete this report?\n\n' +
        'This action cannot be undone.'
    );
}

/**
 * Auto-dismiss success and error alert banners after a delay.
 */
function initAlertDismiss() {
    const alerts = document.querySelectorAll('.alert.alert-success');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 400ms ease';
            alert.style.opacity = '0';
            setTimeout(function () {
                if (alert.parentNode) {
                    alert.parentNode.removeChild(alert);
                }
            }, 420);
        }, 4000);
    });
}

/**
 * Highlight the active navigation link based on the current URL path.
 */
function initNavHighlight() {
    const path = window.location.pathname;
    const links = document.querySelectorAll('.sidebar-nav a');
    links.forEach(function (link) {
        if (link.getAttribute('href') === path) {
            link.classList.add('active');
        }
    });
}

/**
 * Show a loading spinner on the submit button when a form is submitted.
 * Prevents accidental double-submit.
 */
function initFormSubmitGuard() {
    const forms = document.querySelectorAll('form[id$="Form"]');
    forms.forEach(function (form) {
        // Skip the logout and delete forms — they have their own handlers
        if (form.id === 'deleteReportForm') return;

        form.addEventListener('submit', function () {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = submitBtn.textContent.trim() + '…';
            }
        });
    });
}

/**
 * Attach debounced search: triggers form submit automatically
 * when the user stops typing in the search input.
 */
function initSearchDebounce() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;

    let debounceTimer;
    searchInput.addEventListener('input', function () {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function () {
            const form = document.getElementById('searchForm');
            if (form) form.submit();
        }, 600);
    });
}

// ── Initialise on DOM ready ───────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
    initAlertDismiss();
    initNavHighlight();
    initFormSubmitGuard();
    initSearchDebounce();
});
