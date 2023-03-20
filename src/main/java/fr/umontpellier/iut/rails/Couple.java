package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;

public class Couple {

    private Route uneRoute;
    private Couple predecesseur;


    public Couple(Route uneRoute, Couple predecesseur) {
        this.uneRoute = uneRoute;
        this.predecesseur = predecesseur;
    }

    public void mettreAJour(ArrayList<Couple> frontiere, ArrayList<Route> dejaVus) {
        ArrayList<Route> listeRouteFils = uneRoute.genererFilsRoute();
        for (Route r:listeRouteFils) {
            if (!dejaVus.contains(r)){
                Couple c = new Couple(r,this);
                frontiere.add(c);
                dejaVus.add(r);
            }
        }
    }
    /*
    public ArrayList<JeuPuzzle> getListeDeMouvements() {
        ArrayList<JeuPuzzle> res = new ArrayList<JeuPuzzle>();
        Couple prec = this.predecesseur;
        res.add(this.taquin);
        while (prec != null) {
            res.add(prec.getTaquin());
            prec = prec.predecesseur;
        }

        Collections.reverse(res);
        return res;
    }
     */
}
