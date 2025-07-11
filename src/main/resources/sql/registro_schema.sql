-- registro_schema.sql
-- File per la creazione e la pulizia del database registro.

DROP DATABASE IF EXISTS registro;
CREATE DATABASE registro;
USE registro;


CREATE TABLE utente (
id_utente int NOT NULL AUTO_INCREMENT,
email varchar(100) NOT NULL,
password varchar(50) NOT NULL,
cognome varchar(30) NOT NULL,
nome varchar(30) NOT NULL,
PRIMARY KEY (id_utente)
);


CREATE TABLE studente (
  id_studente INT NOT NULL,
  matricola VARCHAR(45) NOT NULL,
  corso_laurea VARCHAR(45) NOT NULL,
  PRIMARY KEY (id_studente),
  UNIQUE (matricola),
  FOREIGN KEY (id_studente) REFERENCES utente(id_utente) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE docente (
  id_docente INT NOT NULL,
  PRIMARY KEY (id_docente),
  FOREIGN KEY (id_docente) REFERENCES utente(id_utente) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE corso (
  id_corso INT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(45) NOT NULL,
  cfu INT NOT NULL,
  id_docente INT NOT NULL,
  PRIMARY KEY (id_corso),
  FOREIGN KEY (id_docente) REFERENCES docente(id_docente) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE iscrizione_corso (
  id_studente INT NOT NULL,
  id_corso INT NOT NULL,
  anno YEAR NOT NULL,
  PRIMARY KEY (id_studente, id_corso, anno),
  FOREIGN KEY (id_studente) REFERENCES studente(id_studente) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (id_corso) REFERENCES corso(id_corso) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE appello (
  id_appello INT NOT NULL AUTO_INCREMENT,
  id_corso INT NOT NULL,
  data DATE NOT NULL,
  PRIMARY KEY (id_appello),
  FOREIGN KEY (id_corso) REFERENCES corso(id_corso) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE verbale (
  id_verbale INT NOT NULL AUTO_INCREMENT,
  codice_verbale INT NOT NULL,
  id_appello INT NOT NULL,
  data_ora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_verbale),
  UNIQUE (codice_verbale),
  FOREIGN KEY (id_appello) REFERENCES appello(id_appello) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE valutazione (
  id_studente INT NOT NULL,
  id_appello INT NOT NULL,
  voto ENUM('', 'assente', 'rimandato', 'riprovato', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '30L') NOT NULL DEFAULT '',
  stato_valutazione ENUM('NON_INSERITO', 'INSERITO', 'PUBBLICATO', 'RIFIUTATO', 'VERBALIZZATO') NOT NULL DEFAULT 'NON_INSERITO',
  id_verbale INT,
  PRIMARY KEY (id_studente, id_appello),
  FOREIGN KEY (id_studente) REFERENCES studente(id_studente) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (id_appello) REFERENCES appello(id_appello) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (id_verbale) REFERENCES verbale(id_verbale) ON DELETE CASCADE ON UPDATE CASCADE
);