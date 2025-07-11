(function() {
	let pageOrchestrator = new PageOrchestrator();

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("email") == null || sessionStorage.getItem("role") != "docente") {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start();
			pageOrchestrator.refresh();
		}
		document.getElementById("username").textContent = sessionStorage.getItem("email");
		document.getElementById("role").textContent = sessionStorage.getItem("role");
		document.getElementById("logoutBtn").addEventListener("click", () => {
			sessionStorage.clear();
			window.location.href = "index.html";
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
			makeCall("GET", "home-docente", null, function(req) {
				if (req.readyState == XMLHttpRequest.DONE) {
					var message = req.responseText;
					if (req.status == 200) {
						var corsiListToShow = JSON.parse(message);
						if (corsiListToShow.length == 0) {
							self.messageContainer.textContent = "No CORSI";
							return;
						}
						self.update(corsiListToShow);
						self.corsiTable.style.visibility = "visible";
					} else {
						self.messageContainer.textContent = message;
					}
				}
			});
		};
		this.update = function(corsiList) {
			this.corsiBody.innerHTML = "";
			corsiList.forEach(corso => {
				let row = document.createElement("tr");
				let cell = document.createElement("td");
				cell.textContent = corso.nome;
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
			makeCall("GET", "home-docente?id_corso=" + corsoId, null, (req) => {
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
							this.appelliBody.appendChild(row);
						});
						this.appelliSection.style.display = "block";
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
			makeCall("GET", "iscritti-appello?id_appello=" + appelloId, null, (req) => {
				if (req.readyState === XMLHttpRequest.DONE) {
					if (req.status === 200) {
						self.iscrittiList = JSON.parse(req.responseText);
						self.renderTable(self.iscrittiList);
						self.iscrittiSection.style.display = "block";
						// Show home button when viewing iscritti
						document.getElementById("homeButton").style.display = "block";
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
				["matricola", "cognome", "nome", "email", "corso_laurea", "voto", "statoDiValutazione"].forEach(field => {
					let cell = document.createElement("td");
					cell.textContent = studente[field] || '-';
					row.appendChild(cell);
				});
				let azioniCell = document.createElement("td");
				if (!["PUBBLICATO", "RIFIUTATO", "VERBALIZZATO"].includes(studente["statoDiValutazione"])) {
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
			
			// Aggiorna lo stato dei pulsanti
			this.updateButtonStates();
		};
		
		this.updateButtonStates = function() {
			const pubblicaButton = document.getElementById("pubblicaButton");
			const verbalizzaButton = document.getElementById("verbalizzaButton");
			const inserimentoMultiploBtn = document.getElementById("inserimentoMultiploBtn");
			
			if (!this.iscrittiList || this.iscrittiList.length === 0) {
				pubblicaButton.disabled = true;
				verbalizzaButton.disabled = true;
				inserimentoMultiploBtn.disabled = true;
				return;
			}
			
			// Conta gli studenti per ogni stato
			const stati = this.iscrittiList.reduce((acc, studente) => {
				const stato = studente.statoDiValutazione;
				acc[stato] = (acc[stato] || 0) + 1;
				return acc;
			}, {});
			
			// Pubblica: abilita se ci sono studenti INSERITO
			pubblicaButton.disabled = !stati["INSERITO"] || stati["INSERITO"] === 0;
			
			// Verbalizza: abilita se ci sono studenti PUBBLICATO o RIFIUTATO
			verbalizzaButton.disabled = !((stati["PUBBLICATO"] && stati["PUBBLICATO"] > 0) || (stati["RIFIUTATO"] && stati["RIFIUTATO"] > 0));
			
			// Inserimento multiplo: abilita se ci sono studenti NON_INSERITO
			inserimentoMultiploBtn.disabled = !stati["NON_INSERITO"] || stati["NON_INSERITO"] === 0;
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
			const nonInseriti = this.iscrittiList.filter(s => s.statoDiValutazione === "NON_INSERITO");
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
					row.setAttribute("data-studente-id", studente.id_studente);
					const fields = ["matricola", "nome", "cognome", "email", "corso_laurea"];
					fields.forEach(field => {
						const cell = document.createElement("td");
						cell.textContent = studente[field] || '-';
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
		// Show home button when modifying student
		document.getElementById("homeButton").style.display = "block";
		makeCall("GET", "inserisci-valutazione?id_studente=" + iscritto.id_studente + "&id_appello=" + iscritto.id_appello, null, function(req) {
			if (req.readyState == XMLHttpRequest.DONE) {
				if (req.status == 200) {
					const studente = JSON.parse(req.responseText);
					document.getElementById("modMatricola").textContent = studente.matricola || '-';
					document.getElementById("modCognome").textContent = studente.cognome || '-';
					document.getElementById("modNome").textContent = studente.nome || '-';
					document.getElementById("modEmail").textContent = studente.email || '-';
					document.getElementById("modLaurea").textContent = studente.corso_laurea || '-';
					document.getElementById("modVoto").value = studente.voto || "";
					document.getElementById("modStato").textContent = studente.statoDiValutazione || '-';
					document.getElementById("modificaStudenteForm").dataset.studenteId = studente.id_studente;
					document.getElementById("modificaStudenteForm").dataset.appelloId = studente.id_appello;
				} else {
					document.getElementById("message").textContent = req.responseText;
				}
			}
		});
	}

	function mostraVerbale(verbale, infoverbalizzati) {
		document.getElementById("corsiSection").style.display = "none";
		document.getElementById("corsiTable").style.display = "none";
		document.getElementById("appelliSection").style.display = "none";
		document.getElementById("iscrittiSection").style.display = "none";
		const verbaleSection = document.getElementById('verbaleSection');
		verbaleSection.style.display = 'block';
		// Show home button when viewing verbale
		document.getElementById("homeButton").style.display = "block";
		
		document.getElementById('verbaleId').textContent = verbale.id_verbale || '-';
		document.getElementById('verbaleData').textContent = verbale.dataVerbale || '-';
		document.getElementById('verbaleOra').textContent = verbale.ora || '-';
		document.getElementById('verbaleDataApp').textContent = verbale.dataAppello || '-';
		
		const tbody = document.getElementById('infoverbalizzatiBody');
		tbody.innerHTML = '';
		infoverbalizzati.forEach(item => {
			let row = document.createElement("tr");
			["matricola", "cognome", "nome", "voto", "statoDiValutazione"].forEach(field => {
				let cell = document.createElement("td");
				cell.textContent = item[field] || '-';
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
		// Show home button when viewing verbali list
		document.getElementById("homeButton").style.display = "block";
		const tbody = document.getElementById("elencoVerbaliBody");
		tbody.innerHTML = '';
		makeCall("GET", "elenco-verbali", null, (req) => {
			if (req.readyState === XMLHttpRequest.DONE) {
				if (req.status === 200) {
					const verbali = JSON.parse(req.responseText);
					verbali.forEach(verbale => {
						let row = document.createElement("tr");
						// Handle the fields from DocenteVerbaleBean
						let cellCorso = document.createElement("td");
						cellCorso.textContent = verbale.nome_corso || verbale.nomeCorso || '-';
						row.appendChild(cellCorso);
						
						let cellDataAppello = document.createElement("td");
						cellDataAppello.textContent = verbale.dataAppello || verbale.data_appello || '-';
						row.appendChild(cellDataAppello);
						
						let cellDataVerbale = document.createElement("td");
						cellDataVerbale.textContent = verbale.dataVerbale || verbale.data_verbale || '-';
						row.appendChild(cellDataVerbale);
						
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
		this.start = function() {
			this.corsi = new Corsi(document.getElementById("corsiTable"), document.getElementById("corsiBody"));
			this.appelli = new Appelli(document.getElementById("appelliSection"), document.getElementById("appelliBody"));
			this.iscritti = new Iscritti(document.getElementById("iscrittiSection"), document.getElementById("iscrittiBody"));
			this.iscritti.attachSortHandlers();
			
			// Home button event handler
			document.getElementById("homeButton").addEventListener("click", () => {
				this.showCorsiSection();
			});
			document.getElementById("verbalizzaButton").addEventListener("click", () => {
				if (this.iscritti.currentAppelloId == null) {
					document.getElementById("message").textContent = "Nessun appello selezionato.";
					return;
				}
				makeCall("POST", "pagina-verbale?id_appello=" + this.iscritti.currentAppelloId, null, (req) => {
					if (req.readyState === XMLHttpRequest.DONE) {
						if (req.status === 200) {
							let response = null;
							try {
								response = JSON.parse(req.responseText);
							} catch (e) {
								document.getElementById("message").textContent = "Errore parsing verbale JSON.";
								return;
							}
							if (response.verbale && response.infoverbalizzati) {
								mostraVerbale(response.verbale, response.infoverbalizzati);
								document.getElementById("message").textContent = "";
							} else {
								document.getElementById("message").textContent = "Errore: risposta non valida dal server.";
							}
						} else {
							document.getElementById("message").textContent = req.responseText;
						}
					}
				});
			});
			document.getElementById("verbaliButton").addEventListener("click", () => {
				mostraElencoVerbali();
			});
			document.getElementById("pubblicaButton").addEventListener("click", () => {
				if (this.iscritti.currentAppelloId == null) {
					document.getElementById("message").textContent = "Nessun appello selezionato.";
					return;
				}
				makeCall("POST", "iscritti-appello?id_appello=" + this.iscritti.currentAppelloId, null, (req) => {
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
			document.getElementById("inserimentoMultiploBtn").addEventListener("click", () => {
				this.iscritti.showInserimentoMultiplo();
			});
			document.getElementById("closeInserimentoModal").addEventListener("click", () => {
				document.getElementById("inserimentoMultiploModal").style.display = "none";
				document.body.style.overflow = "auto";
			});
			document.getElementById("modificaStudenteForm").addEventListener("submit", function(e) {
				e.preventDefault();
				const studenteId = document.getElementById("modificaStudenteForm").dataset.studenteId;
				const appelloId = document.getElementById("modificaStudenteForm").dataset.appelloId;
				const voto = document.getElementById("modVoto").value;
				const validi = ["18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "30L", "30l", "ASSENTE", "RIPROVATO"];
				if (!validi.includes(voto)) {
					document.getElementById("message").textContent = "Voto non valido, riprova.";
					return;
				}
				makeCall("POST", "inserisci-valutazione?id_studente=" + studenteId + "&id_appello=" + appelloId + "&voto=" + voto, null, (req) => {
					if (req.readyState === XMLHttpRequest.DONE) {
						if (req.status === 200) {
							document.getElementById("message").textContent = "Voto modificato con successo.";
							document.getElementById("modificaStudenteSection").style.display = "none";
							pageOrchestrator.iscritti.show(appelloId);
						} else {
							document.getElementById("message").textContent = req.responseText;
						}
					}
				});
			});
			document.getElementById("inserimentoMultiploForm").addEventListener("submit", function(event) {
				event.preventDefault();
				const appelloId = pageOrchestrator.iscritti.currentAppelloId;
				const rows = document.querySelectorAll("#inserimentoMultiploBody tr");
				const requests = Array.from(rows).map(row => {
					const studenteId = row.getAttribute("data-studente-id");
					const votoInput = row.querySelector("input[name='voto']");
					const voto = votoInput.value.trim();
					if (voto !== "") {
						return new Promise((resolve, reject) => {
							makeCall("POST", "inserisci-valutazione?id_studente=" + studenteId + "&id_appello=" + appelloId + "&voto=" + voto, null, (req) => {
								if (req.readyState === XMLHttpRequest.DONE) {
									if (req.status === 200) {
										resolve();
									} else {
										reject(req.status);
									}
								}
							});
						});
					} else {
						return Promise.resolve();
					}
				});
				Promise.all(requests).then(() => {
					pageOrchestrator.iscritti.show(appelloId);
					document.getElementById("inserimentoMultiploModal").style.display = "none";
					document.body.style.overflow = "auto";
				}).catch(error => {
					console.error("Errore durante l'inserimento dei voti:", error);
				});
			});
		};
		this.refresh = function() {
			this.corsi.reset();
			this.corsi.show();
			this.appelli.reset();
			this.iscritti.reset();
		};
		
		this.showCorsiSection = function() {
			// Hide all sections except corsi
			document.getElementById("corsiSection").style.display = "block";
			document.getElementById("corsiTable").style.display = "table";
			document.getElementById("appelliSection").style.display = "none";
			document.getElementById("iscrittiSection").style.display = "none";
			document.getElementById("modificaStudenteSection").style.display = "none";
			document.getElementById("verbaleSection").style.display = "none";
			document.getElementById("elencoVerbaliSection").style.display = "none";
			
			// Hide home button when on main page
			document.getElementById("homeButton").style.display = "none";
			
			// Refresh the course list
			this.corsi.show();
		};
	}

})(); 