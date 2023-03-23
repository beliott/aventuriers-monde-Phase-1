package fr.umontpellier.iut.rails.data;

import fr.umontpellier.iut.rails.Route;

import java.util.ArrayList;

public record Ville(
        String nom,
        boolean estPort) {

    @Override
    public String toString() {
        return nom;
    }

    public String toLog() {
        return String.format("<span class=\"ville\">%s</span>", nom);
    }

    @Override
    public boolean estPort() {
        return estPort;
    }

    public String getNom() {
        return nom;
    }
}
