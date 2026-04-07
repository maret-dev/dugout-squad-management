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

    // Mark All Present per event column
    document.querySelectorAll('.mark-all-present').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            var eventId = btn.getAttribute('data-event');
            document.querySelectorAll('.attendance-cell[data-event="' + eventId + '"]').forEach(function (cell) {
                var input = cell.querySelector('input[type="hidden"]');
                var label = cell.querySelector('.cell-label');
                if (!input || !label) return;
                input.value = 'true';
                label.textContent = 'P';
                cell.classList.add('cell-present');
                cell.classList.remove('cell-absent');
            });
        });
    });

    // Mark All Absent per event column
    document.querySelectorAll('.mark-all-absent').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            var eventId = btn.getAttribute('data-event');
            document.querySelectorAll('.attendance-cell[data-event="' + eventId + '"]').forEach(function (cell) {
                var input = cell.querySelector('input[type="hidden"]');
                var label = cell.querySelector('.cell-label');
                if (!input || !label) return;
                input.value = 'false';
                label.textContent = 'A';
                cell.classList.add('cell-absent');
                cell.classList.remove('cell-present');
            });
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
