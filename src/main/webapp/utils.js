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
    } else {
        req.send();
    }
} 