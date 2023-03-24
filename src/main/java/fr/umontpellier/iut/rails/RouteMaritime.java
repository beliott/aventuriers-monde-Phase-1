package fr.umontpellier.iut.rails;

import fr.umontpellier.iut.rails.data.Couleur;
import fr.umontpellier.iut.rails.data.Ville;

public class RouteMaritime extends Route {
    public RouteMaritime(Ville ville1, Ville ville2, Couleur couleur, int longueur) {
        super(ville1, ville2, couleur, longueur);
    }
    public boolean estMaritime(){
        return true;
    }

    public boolean estTerrestre(){
        return false;
    }

    @Override
    public boolean estPair() {
        return false;
    }
}
