package it.polimi.tiw.beans;

public enum StatoDiValutazione {
    NON_INSERITO("non_inserito"),
    INSERITO("inserito"),
    PUBBLICATO("pubblicato"),
    RIFIUTATO("rifiutato"),
    VERBALIZZATO("verbalizzato");

    private final String label;

    StatoDiValutazione(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}