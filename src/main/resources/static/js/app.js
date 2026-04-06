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
});
