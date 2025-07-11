(function() {
	let pageOrchestrator = new PageOrchestrator();

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("email") == null || sessionStorage.getItem("role") != "studente") {
			window.location.href = "index.html";
		} else {
			//display initial content
			pageOrchestrator.start();
			pageOrchestrator.refresh();
		}
		const nome = sessionStorage.getItem("nome") || "";
		const cognome = sessionStorage.getItem("cognome") || "";
		const nomeCompleto = nome && cognome ? `${nome} ${cognome}` : sessionStorage.getItem("email");
		document.getElementById("username").textContent = nomeCompleto;
		
		// Capitalizza il ruolo per la visualizzazione
		const role = sessionStorage.getItem("role") || "";
		const roleCapitalized = role.charAt(0).toUpperCase() + role.slice(1).toLowerCase();
		const roleElement = document.getElementById("role");
		if (roleElement) {
			roleElement.textContent = roleCapitalized;
		}
		document.getElementById("logoutBtn").addEventListener("click", () => {
			sessionStorage.clear();
			window.location.href = "index.html";
		});
	}, false);

	// Funzione per formattare gli stati di valutazione
	function formatStatoValutazione(stato) {
		if (!stato) return '-';
		return stato.replace('_', ' ');
	}

	function Corsi(_corsiTable, _corsiBody) {
		this.corsiTable = _corsiTable;
		this.corsiBody = _corsiBody;
		this.messageContainer = document.getElementById("message");

		this.reset = function() {
			this.corsiTable.style.visibility = "hidden";
		};
		this.show = function() {
			var self = this;
			makeCall("GET", "home-studente", null,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						if (req.status === 401) {
							window.location.href = "index.html";
							return;
						}
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
				cell.textContent = corso.nome; // Corretto: usa "nome" come nel JSON
				cell.style.textDecoration = "underline";
				cell.style.color = "#007bff";
				row.appendChild(cell);

				row.addEventListener("click", () => {
					pageOrchestrator.appelli.show(corso.id_corso); // Richiama funzione con id corso
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
			let self = this;
			makeCall("GET", "home-studente?id_corso=" + corsoId, null, (req) => {
				if (req.readyState === XMLHttpRequest.DONE) {
					if (req.status === 401) {
						window.location.href = "index.html";
						return;
					}
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
								pageOrchestrator.esito.show(appello.id_appello, corsoId);
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

	function Esito(esitoSection, esitoContent) {
		this.esitoSection = esitoSection;
		this.esitoContent = esitoContent;
		this.rifiutaButton = document.getElementById("rifiutaButton");
		this.trashcan = document.getElementById("trashcan");

		this.currentAppelloId = null;
		this.currentCorsoId = null;

		this.reset = function() {
			this.esitoSection.style.display = "none";
			this.esitoContent.innerHTML = "";
			this.rifiutaButton.style.display = "none";
			this.currentAppelloId = null;
			this.currentCorsoId = null;
			this.trashcan.style.display = "none";
		};

		this.show = function(appelloId, corsoId) {
			this.reset();
			this.currentAppelloId = appelloId;
			this.currentCorsoId = corsoId;

			document.getElementById("corsiSection").style.display = "none";
			document.getElementById("appelliSection").style.display = "none";
			makeCall("GET", "esito?appelloId=" + appelloId + "&id_corso=" + corsoId, null, (req) => {
				if (req.readyState === XMLHttpRequest.DONE) {
					if (req.status === 200) {
						const esito = JSON.parse(req.responseText);
						this.esitoContent.innerHTML = "";

						// Card layout per i dati dell'esito
						const card = document.createElement("div");
						card.style.background = "#f8fafc";
						card.style.borderRadius = "12px";
						card.style.boxShadow = "0 2px 8px rgba(44,62,80,0.07)";
						card.style.padding = "24px 18px";
						card.style.marginBottom = "18px";

						const addRow = (label, value) => {
							const row = document.createElement("div");
							row.style.display = "flex";
							row.style.justifyContent = "space-between";
							row.style.marginBottom = "10px";
							const l = document.createElement("span");
							l.style.fontWeight = "600";
							l.style.color = "#2980b9";
							l.textContent = label;
							const v = document.createElement("span");
							v.textContent = value;
							row.appendChild(l);
							row.appendChild(v);
							card.appendChild(row);
						};

						addRow("Nome corso:", esito.nome_corso);
						addRow("Data appello:", esito.data || "-");
						addRow("Matricola:", esito.matricola);
						addRow("Cognome:", esito.cognome);
						addRow("Nome:", esito.nome);
						addRow("Email:", esito.email);
						addRow("Corso di laurea:", esito.corsolaurea);
						addRow("Voto:", esito.voto != null ? esito.voto : "Non definito");
						addRow("Stato:", formatStatoValutazione(esito.statodivalutazione));

						this.esitoContent.appendChild(card);

						// Mostra pulsante rifiuto solo se il voto è PUBBLICATO e non è già RIFIUTATO
						if (esito.voto != null && esito.statodivalutazione === "PUBBLICATO") {
							this.rifiutaButton.style.display = "inline-block";
							this.rifiutaButton.disabled = false;
							this.trashcan.style.display = "inline-block";
						} else if (esito.statodivalutazione === "RIFIUTATO") {
							this.rifiutaButton.style.display = "none";
							this.trashcan.style.display = "none";
							const msg = document.createElement("p");
							msg.style.color = "#e74c3c";
							msg.style.fontWeight = "bold";
							msg.textContent = "Il voto è stato rifiutato";
							this.esitoContent.appendChild(msg);
						} else {
							this.rifiutaButton.style.display = "none";
							this.trashcan.style.display = "none";
						}

						this.esitoSection.style.display = "block";
						// Mostra il pulsante home
						if (homeBtn) homeBtn.style.display = "block";
					} else {
						document.getElementById("message").textContent = req.responseText;
					}
				}
			});
		};

		// Drag & Drop implementation
		// 1. Set dataTransfer on dragstart
		this.esitoContent.addEventListener("dragstart", (ev) => {
			ev.dataTransfer.setData("text/plain", "esitoContentDragged");
			// You can style the dragging element if you want
		});

		// 2. Allow drop on trashcan
		this.trashcan.addEventListener("dragover", (ev) => {
			ev.preventDefault(); // allow drop
			this.trashcan.style.filter = "brightness(0.8)"; // optional highlight effect
		});

		this.trashcan.addEventListener("dragleave", (ev) => {
			this.trashcan.style.filter = "none"; // remove highlight effect
		});

		// 3. On drop, trigger rifiutaButton click logic
		this.trashcan.addEventListener("drop", (ev) => {
			ev.preventDefault();
			this.trashcan.style.filter = "none";

			// You could also check ev.dataTransfer.getData to confirm
			const data = ev.dataTransfer.getData("text/plain");
			if (data === "esitoContentDragged") {
				// Trigger the rifiuta action programmatically
				document.getElementById("confirmModal").style.display = "flex";
			}
		});

		this.rifiutaButton.addEventListener("click", () => {
			if (!this.currentAppelloId || !this.currentCorsoId) return;

			this.rifiutaButton.disabled = true; // previeni doppio click

			makeCall("POST", "esito?appelloId=" + this.currentAppelloId + "&id_corso=" + this.currentCorsoId, null, (req) => {
				if (req.readyState === XMLHttpRequest.DONE) {
					if (req.status === 200) {
						// Ricarica i dati dell'esito per mostrare lo stato aggiornato
						pageOrchestrator.esito.show(this.currentAppelloId, this.currentCorsoId);
						document.getElementById("message").textContent = "Voto rifiutato con successo!";
						setTimeout(() => {
							document.getElementById("message").textContent = "";
						}, 3000);
					} else {
						document.getElementById("message").textContent = "Errore nel rifiuto del voto: " + req.responseText;
						this.rifiutaButton.disabled = false;
					}
				}
			});
			document.getElementById("confirmModal").style.display = "none";
		});

		document.getElementById("cancelModal").addEventListener("click", () => {
			document.getElementById("confirmModal").style.display = "none";
		});
	}

	function PageOrchestrator() {
		this.corsi = null;
		this.appelli = null;

		this.start = function() {
			this.corsi = new Corsi(document.getElementById("corsiTable"), document.getElementById("corsiBody")
			);
			this.appelli = new Appelli(
				document.getElementById("appelliSection"),
				document.getElementById("appelliBody")
			);

			this.esito = new Esito(
				document.getElementById("esitoSection"),
				document.getElementById("esitoContent")
			);
		};

		this.refresh = function() {
			this.corsi.reset();
			this.corsi.show();
			this.appelli.reset();
		};
	}

	function showCorsiSection() {
		hideAllSections();
		document.getElementById("corsiSection").style.display = "block";
		document.getElementById("message").textContent = "";
		if (pageOrchestrator && pageOrchestrator.corsi) {
			pageOrchestrator.corsi.show();
		}
		const homeBtn = document.getElementById("homeButton");
		if (homeBtn) homeBtn.style.display = "none";
	}

	const homeBtn = document.getElementById("homeButton");
	if (homeBtn) {
		homeBtn.addEventListener("click", () => {
			showCorsiSection();
		});
	}

	function hideAllSections() {
		["corsiSection", "appelliSection", "esitoSection"].forEach(id => {
			const el = document.getElementById(id);
			if (el) el.style.display = "none";
		});
	}

	// Migliora la visualizzazione della pagina EsitoStudente
	// Applica una card e layout più leggibile nella funzione Esito.show
	const esitoSection = document.getElementById("esitoSection");
	if (esitoSection) {
		esitoSection.style.background = "#fff";
		esitoSection.style.borderRadius = "16px";
		esitoSection.style.boxShadow = "0 4px 24px rgba(44,62,80,0.13)";
		esitoSection.style.padding = "32px 24px";
		esitoSection.style.maxWidth = "420px";
		esitoSection.style.margin = "40px auto";
	}

})(); 