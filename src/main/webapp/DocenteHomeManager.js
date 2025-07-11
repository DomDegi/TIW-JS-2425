(function() {
	let pageOrchestrator = new PageOrchestrator();
	let contextPath = '/' + window.location.pathname.split('/')[1];

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("email") == null || sessionStorage.getItem("role") !== "docente") {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start();
			pageOrchestrator.refresh();
		}
		document.getElementById("username").textContent = sessionStorage.getItem("email");
		document.getElementById("logoutBtn").addEventListener("click", () => {
			sessionStorage.clear();
			window.location.href = "index.html";
		});
	}, false);

	document.getElementById("inserimentoMultiploBtn").addEventListener("click", function() {
		const modal = document.getElementById("inserimentoMultiploModal");
		const tbody = document.getElementById("inserimentoMultiploBody");
		tbody.innerHTML = "";
		// Filtra solo NON_INSERITO
		const nonInseriti = pageOrchestrator.iscritti.iscrittiList.filter(s => s.statodivalutazione === "NON_INSERITO");
		if (nonInseriti.length === 0) {
			const row = document.createElement("tr");
			const cell = document.createElement("td");
			cell.setAttribute("colspan", "6");
			cell.textContent = "Nessuno studente nello stato NON_INSERITO";
			row.appendChild(cell);
			tbody.appendChild(row);
		} else {
			nonInseriti.forEach(studente => {
				const row = document.createElement("tr");
				row.setAttribute("data-id_studente", studente.id_studente);
				["matricola", "nome", "cognome", "email", "corsolaurea"].forEach(field => {
					const cell = document.createElement("td");
					cell.textContent = studente[field];
					row.appendChild(cell);
				});
				const votoCell = document.createElement("td");
				const input = document.createElement("input");
				input.type = "text";
				input.name = "voto";
				votoCell.appendChild(input);
				row.appendChild(votoCell);
				tbody.appendChild(row);
			});
		}
		modal.style.display = "flex";
		document.body.style.overflow = "hidden";
	});
	document.getElementById("closeInserimentoModal").addEventListener("click", function() {
		document.getElementById("inserimentoMultiploModal").style.display = "none";
		document.body.style.overflow = "auto";
	});
	document.getElementById("inserimentoMultiploForm").addEventListener("submit", function(event) {
		event.preventDefault();
		const appelloId = pageOrchestrator.iscritti.currentAppelloId;
		const rows = document.querySelectorAll("#inserimentoMultiploBody tr");
		const matricole = [];
		let voto = null;
		rows.forEach(row => {
			const id_studente = row.getAttribute("data-id_studente");
			const input = row.querySelector("input[name='voto']");
			if (input.value.trim() !== "") {
				matricole.push(id_studente);
				voto = input.value.trim();
			}
		});
		if (matricole.length === 0) return;
		makeCall("POST", contextPath + "/inserisci-valutazione",
			`matricole=${encodeURIComponent(matricole.join(","))}&id_appello=${appelloId}&voto=${encodeURIComponent(voto)}`,
			function(req) {
				if (req.readyState === XMLHttpRequest.DONE && req.status === 200) {
					pageOrchestrator.iscritti.show(appelloId);
					document.getElementById("inserimentoMultiploModal").style.display = "none";
					document.body.style.overflow = "auto";
				}
			}
		);
	});

	document.getElementById("pubblicaButton").addEventListener("click", function() {
		let appelloId = pageOrchestrator.iscritti.currentAppelloId;
		if (!appelloId) return;
		makeCall("POST", contextPath + "/pubblica-voti", `id_appello=${appelloId}`, function(req) {
			if (req.readyState === XMLHttpRequest.DONE && req.status === 200) {
				pageOrchestrator.iscritti.show(appelloId);
				document.getElementById("pubblicaButton").disabled = true;
				document.getElementById("inserimentoMultiploBtn").disabled = true;
			}
		});
	});

	document.getElementById("verbalizzaButton").addEventListener("click", function() {
		let appelloId = pageOrchestrator.iscritti.currentAppelloId;
		if (!appelloId) return;
		makeCall("POST", contextPath + "/verbalizza", `id_appello=${appelloId}`, function(req) {
			if (req.readyState === XMLHttpRequest.DONE && req.status === 200) {
				const verbale = JSON.parse(req.responseText);
				mostraVerbale(verbale);
				document.getElementById("verbalizzaButton").disabled = true;
				document.getElementById("pubblicaButton").disabled = true;
				document.getElementById("inserimentoMultiploBtn").disabled = true;
			}
		});
	});

	function Corsi(_corsiTable, _corsiBody) {
		this.corsiTable = _corsiTable;
		this.corsiBody = _corsiBody;
		this.messageContainer = document.getElementById("message");

		this.reset = function() {
			this.corsiTable.style.visibility = "hidden";
		};
		this.show = function() {
			var self = this;
			makeCall("GET", contextPath + "/home-docente", null,
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
				cell.textContent = corso.nome;
				cell.classList.add("cliccabile");
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
			makeCall("GET", contextPath + "/home-docente?id_corso=" + corsoId, null, (req) => {
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
							cell.classList.add("cliccabile");
							cell.style.textDecoration = "underline";
							cell.style.color = "#007bff";
							cell.addEventListener("click", () => {
								pageOrchestrator.iscritti.show(appello.id_appello);
							});
							row.appendChild(cell);
							this.appelliBody.appendChild(row);
							this.appelliSection.style.display = "block";
						});
					} else {
						document.getElementById("message").textContent = req.responseText;
					}
				}
			});
		};
	}

	function Verbali(verbaliSection, verbaliBody) {
		this.verbaliSection = verbaliSection;
		this.verbaliBody = verbaliBody;

		this.reset = function() {
			this.verbaliSection.style.display = "none";
			this.verbaliBody.innerHTML = "";
		};

		this.show = function() {
			this.reset();
			makeCall("GET", contextPath + "/elenco-verbali", null, (req) => {
				if (req.readyState === XMLHttpRequest.DONE) {
					if (req.status === 401) {
						window.location.href = "index.html";
						return;
					}
					if (req.status === 200) {
						let verbali = JSON.parse(req.responseText);
						verbali.forEach(verbale => {
							let row = document.createElement("tr");
							let cellCorso = document.createElement("td");
							cellCorso.textContent = verbale.nome_corso || verbale.nomeCorso || verbale.nome || "-";
							row.appendChild(cellCorso);
							let cellDataAppello = document.createElement("td");
							cellDataAppello.textContent = verbale.data_appello || verbale.dataAppello || verbale.data || "-";
							row.appendChild(cellDataAppello);
							let cellDataVerbale = document.createElement("td");
							cellDataVerbale.textContent = verbale.data_verbale || verbale.dataVerbale || "-";
							row.appendChild(cellDataVerbale);
							this.verbaliBody.appendChild(row);
						});
						this.verbaliSection.style.display = "block";
					} else {
						document.getElementById("message").textContent = req.responseText;
					}
				}
			});
		};
	}

	function Iscritti(iscrittiSection, iscrittiBody) {
		this.iscrittiSection = iscrittiSection;
		this.iscrittiBody = iscrittiBody;
		this.currentAppelloId = null;
		this.iscrittiList = [];
		this.currentSort = { key: null, ascending: true };

		this.reset = function() {
			this.iscrittiSection.style.display = "none";
			this.iscrittiBody.innerHTML = "";
			this.currentAppelloId = null;
			this.iscrittiList = [];
			this.currentSort = { key: null, ascending: true };
		};

		this.show = function(appelloId) {
			this.reset();
			this.currentAppelloId = appelloId;
			document.getElementById("corsiTable").style.display = "none";
			document.getElementById("corsiSection").style.display = "none";
			document.getElementById("appelliSection").style.display = "none";
			let self = this;
			makeCall("GET", contextPath + "/home-docente?id_appello=" + appelloId, null, (req) => {
				if (req.readyState === XMLHttpRequest.DONE) {
					if (req.status === 200) {
						self.iscrittiList = JSON.parse(req.responseText);
						self.renderTable(self.iscrittiList);
						self.iscrittiSection.style.display = "block";
						self.attachSortHandlers();
					} else {
						document.getElementById("message").textContent = req.responseText;
					}
				}
			});
		};

		this.renderTable = function(list) {
			this.iscrittiBody.innerHTML = "";
			list.forEach(studente => {
				let row = document.createElement("tr");
				["matricola", "cognome", "nome", "email", "corsolaurea", "voto", "statodivalutazione"].forEach(field => {
					let cell = document.createElement("td");
					cell.textContent = studente[field];
					row.appendChild(cell);
				});
				// Colonna Azioni
				let azioniCell = document.createElement("td");
				if (!["PUBBLICATO", "RIFIUTATO", "VERBALIZZATO"].includes(studente["statodivalutazione"])) {
					let modificaButton = document.createElement("button");
					modificaButton.textContent = "MODIFICA";
					modificaButton.addEventListener("click", () => {
						document.getElementById("corsiTable").style.display = "none";
						document.getElementById("corsiSection").style.display = "none";
						document.getElementById("appelliSection").style.display = "none";
						document.getElementById("iscrittiSection").style.display = "none";
						mostraModificaStudente(studente);
					});
					azioniCell.appendChild(modificaButton);
				}
				row.appendChild(azioniCell);
				this.iscrittiBody.appendChild(row);
			});
		};

		this.sortBy = function(key) {
			if (this.currentSort.key === key) {
				this.currentSort.ascending = !this.currentSort.ascending;
			} else {
				this.currentSort.key = key;
				this.currentSort.ascending = true;
			}
			const ascending = this.currentSort.ascending;
			this.iscrittiList.sort((a, b) => {
				let valA = a[key];
				let valB = b[key];
				let numA = parseFloat(valA);
				let numB = parseFloat(valB);
				if (!isNaN(numA) && !isNaN(numB)) {
					valA = numA;
					valB = numB;
				} else {
					if (typeof valA === "string") valA = valA.toLowerCase();
					if (typeof valB === "string") valB = valB.toLowerCase();
				}
				if (valA < valB) return ascending ? -1 : 1;
				if (valA > valB) return ascending ? 1 : -1;
				return 0;
			});
			this.renderTable(this.iscrittiList);
		};

		this.attachSortHandlers = function() {
			const headers = this.iscrittiSection.querySelectorAll("thead a[data-order]");
			headers.forEach(header => {
				header.addEventListener("click", (e) => {
					e.preventDefault();
					const key = header.getAttribute("data-order");
					this.sortBy(key);
				});
			});
		};

		this.showInserimentoMultiplo = function() {
			const modal = document.getElementById("inserimentoMultiploModal");
			const tbody = document.getElementById("inserimentoMultiploBody");
			tbody.innerHTML = "";
			// filtra iscritti che hanno lo stato di valutazione a non inserito
			const nonInseriti = this.iscrittiList.filter(s => s.statodivalutazione === "NON_INSERITO");
			if (nonInseriti.length === 0) {
				const row = document.createElement("tr");
				const cell = document.createElement("td");
				cell.setAttribute("colspan", "6");
				cell.textContent = "Nessuno studente nello stato NON_INSERITO";
				row.appendChild(cell);
				tbody.appendChild(row);
			} else {
				nonInseriti.forEach(studente => {
					const row = document.createElement("tr");
					row.setAttribute("data-id_studente", studente.id_studente);
					const fields = ["matricola", "nome", "cognome", "email", "corsolaurea"];
					fields.forEach(field => {
						const cell = document.createElement("td");
						cell.textContent = studente[field];
						row.appendChild(cell);
					});
					const votoCell = document.createElement("td");
					const input = document.createElement("input");
					input.type = "text";
					input.name = "voto";
					votoCell.appendChild(input);
					row.appendChild(votoCell);
					tbody.appendChild(row);
				});
			}
			modal.style.display = "flex";
			document.body.style.overflow = "hidden";
		};
	}

	function mostraModificaStudente(iscritto) {
		document.getElementById("message").textContent = "";
		document.getElementById("corsiSection").style.display = "none";
		document.getElementById("appelliSection").style.display = "none";
		document.getElementById("iscrittiSection").style.display = "none";
		document.getElementById("modificaStudenteSection").style.display = "block";
		// Chiamata AJAX per popolare il form
		makeCall("GET", contextPath + "/inserisci-valutazione?id_studente=" + iscritto.id_studente + "&id_appello=" + iscritto.id_appello, null,
			function(req) {
				if (req.readyState == XMLHttpRequest.DONE) {
					if (req.status == 200) {
						const studente = JSON.parse(req.responseText);
						document.getElementById("modMatricola").textContent = studente.matricola;
						document.getElementById("modCognome").textContent = studente.cognome;
						document.getElementById("modNome").textContent = studente.nome;
						document.getElementById("modEmail").textContent = studente.email;
						document.getElementById("modLaurea").textContent = studente.corsolaurea;
						document.getElementById("modVoto").value = studente.voto || "";
						document.getElementById("modStato").textContent = studente.statodivalutazione;
						document.getElementById("modificaStudenteForm").dataset.id_studente = studente.id_studente;
						document.getElementById("modificaStudenteForm").dataset.id_appello = studente.id_appello;
					} else {
						document.getElementById("message").textContent = req.responseText;
					}
				}
			}
		);
	}

	function mostraVerbale(verbale, infoverbalizzati) {
		document.getElementById("corsiSection").style.display = "none";
		document.getElementById("corsiTable").style.display = "none";
		document.getElementById("appelliSection").style.display = "none";
		document.getElementById("iscrittiSection").style.display = "none";

		const verbaleSection = document.getElementById('verbaleSection');
		verbaleSection.style.display = 'block';

		document.getElementById('verbaleId').textContent = verbale.id_verbale || '-';
		document.getElementById('verbaleData').textContent = verbale.data_verbale || '-';
		document.getElementById('verbaleOra').textContent = verbale.ora_verbale || '-';
		document.getElementById('verbaleDataApp').textContent = verbale.data_appello || '-';

		const tbody = document.getElementById('infoverbalizzatiBody');
		tbody.innerHTML = '';

		infoverbalizzati.forEach(item => {
			let row = document.createElement("tr");
			["matricola", "cognome", "nome", "voto", "statodivalutazione"].forEach(field => {
				let cell = document.createElement("td");
				cell.textContent = item[field];
				row.appendChild(cell);
			});
			tbody.appendChild(row);
		});
	}

	function mostraElencoVerbali() {
		document.getElementById("corsiSection").style.display = "none";
		document.getElementById("corsiTable").style.display = "none";
		document.getElementById("appelliSection").style.display = "none";
		document.getElementById("iscrittiSection").style.display = "none";
		document.getElementById("message").textContent = "";

		document.getElementById("elencoVerbaliSection").style.display = "block";

		makeCall("GET", contextPath + "/elenco-verbali", null, (req) => {
			if (req.readyState === XMLHttpRequest.DONE) {
				if (req.status === 200) {
					const verbali = JSON.parse(req.responseText);
					const tbody = document.getElementById("elencoVerbaliBody");
					tbody.innerHTML = '';
					verbali.forEach(verbale => {
						let row = document.createElement("tr");
						["nome_corso", "data_appello", "data_verbale"].forEach(field => {
							let cell = document.createElement("td");
							cell.textContent = verbale[field];
							row.appendChild(cell);
						});
						tbody.appendChild(row);
					});
				} else {
					document.getElementById("message").textContent = req.responseText;
				}
			}
		});
	}

	function PageOrchestrator() {
		this.corsi = null;
		this.appelli = null;
		this.iscritti = null;
		this.verbali = null;

		this.start = function() {
			this.corsi = new Corsi(document.getElementById("corsiTable"), document.getElementById("corsiBody"));
			this.appelli = new Appelli(document.getElementById("appelliSection"), document.getElementById("appelliBody"));
			this.iscritti = new Iscritti(document.getElementById("iscrittiSection"), document.getElementById("iscrittiBody"));
			this.iscritti.attachSortHandlers();

			// VERBALIZZA
			document.getElementById("verbalizzaButton").addEventListener("click", () => {
				if (this.iscritti.currentAppelloId == null) {
					document.getElementById("message").textContent = "Nessun appello selezionato.";
					return;
				}
				makeCall("POST", contextPath + "/verbalizza", "id_appello=" + this.iscritti.currentAppelloId, (req) => {
					if (req.readyState === XMLHttpRequest.DONE) {
						if (req.status === 200) {
							let response = null;
							try {
								response = JSON.parse(req.responseText);
							} catch (e) {
								document.getElementById("message").textContent = "Errore parsing verbale JSON.";
								return;
							}
							mostraVerbale(response.verbale, response.infoverbalizzati);
							document.getElementById("message").textContent = "";
						} else {
							document.getElementById("message").textContent = req.responseText;
						}
					}
				});
			});

			// VERBALI
			document.getElementById("verbaliButton").addEventListener("click", () => {
				mostraElencoVerbali();
			});

			// PUBBLICA
			document.getElementById("pubblicaButton").addEventListener("click", () => {
				if (this.iscritti.currentAppelloId == null) {
					document.getElementById("message").textContent = "Nessun appello selezionato.";
					return;
				}
				makeCall("POST", contextPath + "/pubblica-voti", "id_appello=" + this.iscritti.currentAppelloId, (req) => {
					if (req.readyState === XMLHttpRequest.DONE) {
						if (req.status === 200) {
							this.iscritti.show(this.iscritti.currentAppelloId);
							document.getElementById("message").textContent = "Voti pubblicati con successo.";
						} else {
							document.getElementById("message").textContent = req.responseText;
						}
					}
				});
			});

			// INSERIMENTO MULTIPLO
			document.getElementById("inserimentoMultiploBtn").addEventListener("click", () => {
				this.iscritti.showInserimentoMultiplo();
			});

			// CHIUDI MODALE
			document.getElementById("closeInserimentoModal").addEventListener("click", () => {
				document.getElementById("inserimentoMultiploModal").style.display = "none";
				document.body.style.overflow = "auto";
			});

			// MODIFICA STUDENTE
			document.getElementById("modificaStudenteForm").addEventListener("submit", function(e) {
				e.preventDefault();
				const id_studente = document.getElementById("modificaStudenteForm").dataset.id_studente;
				const id_appello = document.getElementById("modificaStudenteForm").dataset.id_appello;
				const voto = document.getElementById("modVoto").value;

				const validi = ["18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "30L", "30l", "ASSENTE", "RIPROVATO"];

				if (!validi.includes(voto)) {
					document.getElementById("message").textContent = "Voto non valido, riprova.";
					return;
				}

				makeCall("POST", contextPath + "/inserisci-valutazione?id_studente=" + id_studente + "&id_appello=" + id_appello + "&voto=" + voto, null, (req) => {
					if (req.readyState === XMLHttpRequest.DONE) {
						if (req.status === 200) {
							document.getElementById("message").textContent = "Voto modificato con successo.";
							document.getElementById("modificaStudenteSection").style.display = "none";
							pageOrchestrator.iscritti.show(id_appello);
						} else {
							document.getElementById("message").textContent = req.responseText;
						}
					}
				});
			});

			// INSERIMENTO MULTIPLO SUBMIT
			document.getElementById("inserimentoMultiploForm").addEventListener("submit", function(event) {
				event.preventDefault();

				const id_appello = pageOrchestrator.iscritti.currentAppelloId;
				const rows = document.querySelectorAll("#inserimentoMultiploBody tr");
				const matricole = [];
				let voto = null;

				rows.forEach(row => {
					const id_studente = row.getAttribute("data-id_studente");
					const votoInput = row.querySelector("input[name='voto']");
					const votoValue = votoInput.value.trim();

					if (votoValue !== "") {
						matricole.push(id_studente);
						voto = votoValue;
					}
				});

				if (matricole.length === 0) return;

				makeCall("POST", contextPath + "/inserisci-valutazione",
					`matricole=${encodeURIComponent(matricole.join(","))}&id_appello=${id_appello}&voto=${encodeURIComponent(voto)}`,
					function(req) {
						if (req.readyState === XMLHttpRequest.DONE && req.status === 200) {
							pageOrchestrator.iscritti.show(id_appello);
							document.getElementById("inserimentoMultiploModal").style.display = "none";
							document.body.style.overflow = "auto";
						}
					}
				);
			});
		};

		this.refresh = function() {
			this.corsi.reset();
			this.corsi.show();
			this.appelli.reset();
			this.iscritti.reset();
		};
	}
})(); 