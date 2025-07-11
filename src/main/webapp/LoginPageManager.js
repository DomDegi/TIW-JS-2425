(function() {
    document.getElementById("loginbutton").addEventListener("click", (e) => {
        var form = document.getElementById("loginform");
        if (form.checkValidity()) {
            makeCall("POST", "login", form, function(req) {
                if (req.readyState == XMLHttpRequest.DONE) {
                    if (req.status == 200) {
                        var userData = JSON.parse(req.responseText);
                        sessionStorage.setItem('email', userData.email || userData.mail || userData.username);
                        sessionStorage.setItem('role', userData.role || userData.tipo || '');
                        // Redirect in base al ruolo
                        if (userData.role === "studente" || userData.tipo === "studente") {
                            window.location.href = "home-studente.html";
                        } else if (userData.role === "docente" || userData.tipo === "docente") {
                            window.location.href = "home-docente.html";
                        } else {
                            document.getElementById("error_message").textContent = "Ruolo sconosciuto.";
                        }
                    } else if (req.status == 400) {
                        document.getElementById("error_message").textContent = JSON.parse(req.responseText).error || req.responseText;
                    } else if (req.status == 401) {
                        document.getElementById("error_message").textContent = JSON.parse(req.responseText).error || req.responseText;
                    } else if (req.status == 500) {
                        document.getElementById("error_message").textContent = JSON.parse(req.responseText).error || req.responseText;
                    } else {
                        document.getElementById("error_message").textContent = req.responseText;
                    }
                }
            });
        } else {
            form.reportValidity();
        }
    });
})(); 