USE registro;

INSERT INTO utente (id_utente, email, password, cognome, nome) VALUES
(1, 'mario.rossi@polimi.it', '1234', 'Rossi', 'Mario'),       -- docente
(2, 'giulia.verdi@polimi.it', '5678', 'Verdi', 'Giulia'),     -- studente 1
(3, 'luca.bianchi@polimi.it', '91011', 'Bianchi', 'Luca'),    -- studente 2
(4, 'anna.neri@polimi.it', 'abcd', 'Neri', 'Anna'),           -- studente 3
(5, 'francesco.ferri@polimi.it', 'qwerty', 'Ferri', 'Francesco'), -- studente 4
(6, 'sara.rossi@polimi.it', 'pass123', 'Rossi', 'Sara'),      -- docente 2
(7, 'fabio.conti@polimi.it', 'secret', 'Conti', 'Fabio'),     -- studente 5
(8, 'elena.mori@polimi.it', 'elena123', 'Mori', 'Elena'),     -- studente 6
(9, 'paolo.rossi@polimi.it', 'paolo123', 'Rossi', 'Paolo'),   -- studente 7
(10, 'chiara.bianchi@polimi.it', 'chiara123', 'Bianchi', 'Chiara'), -- studente 8
(11, 'filippo.wang@polimi.it', 'filippo123', 'Wang', 'Filippo'), -- docente 3
(12, 'domenico.degiorgio@polimi.it', 'domenico123', 'De Giorgio', 'Domenico'); -- studente 9


INSERT INTO docente (id_docente) VALUES
(1),  -- Mario Rossi
(6),  -- Sara Rossi
(11); -- Filippo Wang


INSERT INTO studente (id_studente, matricola, corso_laurea) VALUES
(2, 'S12345', 'Informatica'),
(3, 'S67890', 'Informatica'),
(4, 'S54321', 'Fisica'),
(5, 'S99999', 'Matematica'),
(7, 'S11223', 'Informatica'),
(8, 'S88888', 'Matematica'),
(9, 'S77777', 'Informatica'),
(10, 'S66666', 'Fisica'),
(12, 'S10101', 'High Performance Computing');


INSERT INTO corso (id_corso, nome, cfu, id_docente) VALUES
(1, 'Tecnologie Informatiche per il Web', 6, 1),
(2, 'Algoritmi e Strutture Dati', 9, 1),
(3, 'Fisica 1', 12, 6),
(4, 'Analisi Matematica 1', 9, 6),
(5, 'Analisi Matematica 2', 9, 6),
(6, 'Chimica', 9, 6),
(7, 'Quantum Computing', 5, 11);

INSERT INTO iscrizione_corso (id_studente, id_corso, anno) VALUES
(2, 1, 2024),
(3, 1, 2024),
(3, 3, 2024),
(5, 1, 2024),
(7, 1, 2024),
(2, 2, 2024),
(4, 3, 2024),
(8, 1, 2024),
(8, 3, 2024),
(9, 1, 2024),
(9, 2, 2024),
(10, 3, 2024),
(12, 7, 2024);


INSERT INTO appello (id_appello, id_corso, data) VALUES
(1, 1, '2025-06-15'),
(2, 1, '2025-07-01'),
(3, 2, '2025-06-20'),
(4, 3, '2025-06-10'),
(5, 1, '2025-09-05'),
(6, 2, '2025-01-16'),
(7, 2, '2025-02-11'),
(8, 2, '2025-06-16'),
(9, 2, '2025-07-11'),
(10, 2, '2025-09-06'),
(11, 3, '2025-01-17'),
(12, 3, '2025-02-12'),
(13, 3, '2025-06-17'),
(14, 3, '2025-07-12'),
(15, 3, '2025-09-07');

INSERT INTO verbale (id_verbale, codice_verbale, id_appello) VALUES
(1, 101, 1),
(2, 102, 2),
(3, 201, 3),
(4, 301, 4);


INSERT INTO valutazione (id_studente, id_appello, voto, stato_valutazione, id_verbale) VALUES
-- Tecnologie Web (corso 1)
(2, 1, '28', 'VERBALIZZATO', 1),      -- Giulia Verdi
(2, 2, '', 'NON_INSERITO', NULL),
(2, 5, '', 'NON_INSERITO', NULL),
(3, 1, '', 'NON_INSERITO', NULL),
(3, 2, '30L', 'PUBBLICATO', NULL),    -- Luca Bianchi (BOTTONE RIFIUTA VISIBILE)
(3, 5, '', 'NON_INSERITO', NULL),
(5, 1, 'assente', 'VERBALIZZATO', 1), -- Francesco Ferri
(5, 2, '', 'NON_INSERITO', NULL),
(5, 5, '', 'NON_INSERITO', NULL),
(7, 1, '', 'NON_INSERITO', NULL),
(7, 2, '29', 'VERBALIZZATO', 2),      -- Fabio Conti
(7, 5, '', 'NON_INSERITO', NULL),
(8, 1, '25', 'PUBBLICATO', NULL),     -- Elena Mori (BOTTONE RIFIUTA VISIBILE)
(9, 1, '', 'NON_INSERITO', NULL),
(9, 2, '28', 'PUBBLICATO', NULL),     -- Paolo Rossi (BOTTONE RIFIUTA VISIBILE)

-- Algoritmi e Strutture Dati (corso 2)
(2, 3, '24', 'VERBALIZZATO', 3),      -- Giulia Verdi
(2, 6, '', 'NON_INSERITO', NULL),
(2, 7, '', 'NON_INSERITO', NULL),
(2, 8, '', 'NON_INSERITO', NULL),
(2, 9, '', 'NON_INSERITO', NULL),
(2, 10, '', 'NON_INSERITO', NULL),
(9, 3, '26', 'INSERITO', NULL),       -- Paolo Rossi (BOTTONE RIFIUTA VISIBILE)
(9, 6, '', 'NON_INSERITO', NULL),
(9, 7, '', 'NON_INSERITO', NULL),
(9, 8, '', 'NON_INSERITO', NULL),
(9, 9, '', 'NON_INSERITO', NULL),
(9, 10, '', 'NON_INSERITO', NULL),

-- Fisica 1 (corso 3)
(4, 4, '19', 'VERBALIZZATO', 4),      -- Anna Neri
(4, 11, '', 'NON_INSERITO', NULL),
(4, 12, '', 'NON_INSERITO', NULL),
(4, 13, '', 'NON_INSERITO', NULL),
(4, 14, '', 'NON_INSERITO', NULL),
(4, 15, '', 'NON_INSERITO', NULL),
(8, 4, '22', 'INSERITO', NULL),       -- Elena Mori (BOTTONE RIFIUTA VISIBILE)
(8, 11, '', 'NON_INSERITO', NULL),
(8, 12, '', 'NON_INSERITO', NULL),
(8, 13, '', 'NON_INSERITO', NULL),
(8, 14, '', 'NON_INSERITO', NULL),
(8, 15, '', 'NON_INSERITO', NULL),
(10, 4, '30L', 'PUBBLICATO', NULL),   -- Chiara Bianchi (BOTTONE RIFIUTA VISIBILE)
(10, 11, '', 'NON_INSERITO', NULL),
(10, 12, '', 'NON_INSERITO', NULL),
(10, 13, '', 'NON_INSERITO', NULL),
(10, 14, '', 'NON_INSERITO', NULL),
(10, 15, '', 'NON_INSERITO', NULL);

-- Quantum Computing (corso 7, solo iscrizione, nessun appello/valutazione ancora)