package fr.umontpellier.iut.rails.data;

import fr.umontpellier.iut.rails.Route;

import java.util.ArrayList;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ville ville = (Ville) o;
        return Objects.equals(this.nom, ville.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom, estPort);
    }
}
