// Dugout - Main JavaScript

document.addEventListener('DOMContentLoaded', function () {

    // Attendance grid toggle
    document.querySelectorAll('.attendance-cell').forEach(function (cell) {
        cell.addEventListener('click', function () {
            var input = cell.querySelector('input[type="hidden"]');
            var label = cell.querySelector('.cell-label');
            if (!input || !label) return;

            var isPresent = input.value === 'true';
            input.value = isPresent ? 'false' : 'true';
            label.textContent = isPresent ? 'A' : 'P';
            cell.classList.toggle('cell-present', !isPresent);
            cell.classList.toggle('cell-absent', isPresent);
        });
    });

    // Flash message auto-dismiss (4 seconds)
    document.querySelectorAll('.alert-dismissible').forEach(function (alert) {
        setTimeout(function () {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            bsAlert.close();
        }, 4000);
    });

    // Confirm dialog for destructive actions
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (!confirm(form.getAttribute('data-confirm'))) {
                e.preventDefault();
            }
        });
    });
});
