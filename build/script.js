// script.js

// Liest einen Query-Parameter aus der URL aus.
function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

// Beispiel: Falls ein Element mit der ID "greeting" existiert, wird es beim Laden
// mit "Hi <benutzername>" aktualisiert (Query-Parameter "benutzername").
window.onload = function() {
    const greetingElement = document.getElementById('greeting');
    if (greetingElement) {
        const username = getQueryParam('benutzername') || 'User';
        greetingElement.textContent = 'Hi ' + username;
    }
};
