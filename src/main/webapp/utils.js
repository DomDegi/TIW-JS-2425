function makeCall(method, url, formElement, callback) {
    const req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        callback(req);
    };

    req.open(method, url, true);
    req.withCredentials = true;

    if (formElement instanceof HTMLFormElement) {
        // Serializza i dati come x-www-form-urlencoded
        const formData = new FormData(formElement);
        const params = new URLSearchParams(formData).toString();
        req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        req.send(params);
    } else if (typeof formElement === "string" && formElement !== null) {
        // Se è una stringa, la invia direttamente
        req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        req.send(formElement);
    } else {
        // Se è null o undefined, non invia dati
        req.send();
    }
}

function addUniqueNavButton(container, buttonId, buttonText, clickHandler) {
    // Rimuovi il pulsante esistente se presente
    const existingButton = document.getElementById(buttonId);
    if (existingButton) {
        existingButton.remove();
    }
    
    const button = document.createElement("button");
    button.id = buttonId;
    button.textContent = buttonText;
    button.style.marginBottom = "10px";
    button.style.padding = "8px 16px";
    button.style.backgroundColor = "#007bff";
    button.style.color = "white";
    button.style.border = "none";
    button.style.borderRadius = "4px";
    button.style.cursor = "pointer";
    
    button.addEventListener("click", clickHandler);
    
    // Inserisci il pulsante all'inizio del container
    if (container.firstChild) {
        container.insertBefore(button, container.firstChild);
    } else {
        container.appendChild(button);
    }
} 