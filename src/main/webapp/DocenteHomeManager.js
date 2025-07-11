(function() {
	let pageOrchestrator = new PageOrchestrator();

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("email") == null || !(sessionStorage.getItem("role") === undefined || sessionStorage.getItem("role") === null || sessionStorage.getItem("role") === "docente")) {
			window.location.href = "../login.html";
		} else {
			pageOrchestrator.start();
			pageOrchestrator.refresh();
		}
		document.getElementById("username").textContent = sessionStorage.getItem("email");
		document.getElementById("logoutBtn").addEventListener("click", () => {
			sessionStorage.clear();
			window.location.href = "../login.html";
		});
	}, false);

	function Corsi(_corsiTable, _corsiBody) {
		this.corsiTable = _corsiTable;
		this.corsiBody = _corsiBody;
		this.messageContainer = document.getElementById("message");

		this.reset = function() {
			this.corsiTable.style.visibility = "hidden";
		};
		this.show = function() {
			var self = this;
			makeCall("GET", "../home-docente", null,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						if (req.status == 200) {
							var corsiListToShow = JSON.parse(message);
							if (corsiListToShow.length == 0) {
								self.messageContainer.textContent = "Nessun corso disponibile.";
								return;
							}

							self.update(corsiListToShow);
							self.corsiTable.style.visibility = "visible";

						} else {
							self.messageContainer.textContent = message;
						}
					}
				}
			);
		};

		this.update = function(corsiList) {
			this.corsiBody.innerHTML = ""; // Pulisce righe precedenti
			corsiList.forEach(corso => {
				let row = document.createElement("tr");
				let cell = document.createElement("td");
				cell.textContent = corso.nome_corso;
				cell.style.textDecoration = "underline";
				cell.style.color = "#007bff";
				row.appendChild(cell);

				row.addEventListener("click", () => {
					pageOrchestrator.appelli.show(corso.id_corso);
				});

				this.corsiBody.appendChild(row);
			});
		};
	}

	function Appelli(appelliSection, appelliBody) {
		this.appelliSection = appelliSection;
		this.appelliBody = appelliBody;

		this.reset = function() {
			this.appelliSection.style.display = "none";
			this.appelliBody.innerHTML = "";
		};
		this.show = function(corsoId) {
			this.reset();
			makeCall("GET", "../home-docente?id_corso=" + corsoId, null, (req) => {
				if (req.readyState === XMLHttpRequest.DONE) {
					if (req.status === 200) {
						let appelli = JSON.parse(req.responseText);
						appelli.forEach(appello => {
							let row = document.createElement("tr");
							let cell = document.createElement("td");
							cell.textContent = appello.data;
							cell.style.textDecoration = "underline";
							cell.style.color = "#007bff";
							row.appendChild(cell);

							row.addEventListener("click", () => {
								pageOrchestrator.iscritti.show(appello.id_appello);
							});

							self.appelliBody.appendChild(row);
						});
						self.appelliSection.style.display = "block";
					} else {
						document.getElementById("message").textContent = req.responseText;
					}
				}
			});
		};
	}

	// ...
	// (Per brevità, qui puoi aggiungere la logica per Iscritti, Verbali, Modifica Studente, ecc.,
	// adattando i nomi dei campi come fatto sopra)

	function PageOrchestrator() {
		this.corsi = null;
		this.appelli = null;
		this.iscritti = null;

		this.start = function() {
			this.corsi = new Corsi(document.getElementById("corsiTable"), document.getElementById("corsiBody"));
			this.appelli = new Appelli(document.getElementById("appelliSection"), document.getElementById("appelliBody"));
			// this.iscritti = new Iscritti(...); // Da implementare come sopra
		};

		this.refresh = function() {
			this.corsi.reset();
			this.corsi.show();
			this.appelli.reset();
			// this.iscritti.reset();
		};
	}

})(); 