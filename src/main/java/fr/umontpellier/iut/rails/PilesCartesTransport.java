package fr.umontpellier.iut.rails;

import fr.umontpellier.iut.rails.data.CarteTransport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PilesCartesTransport {
    private final List<CarteTransport> pilePioche;
    private final List<CarteTransport> pileDefausse;

    public PilesCartesTransport(List<CarteTransport> cartes) {
        pilePioche = cartes;
        pileDefausse = new ArrayList<>();
        // on mélange la pile de pioche
        Collections.shuffle(pilePioche);
    }

    public List<CarteTransport> getPilePioche() {
        return pilePioche;
    }

    public List<CarteTransport> getPileDefausse() {
        return pileDefausse;
    }

    /**
     * Retire une carte. Si la pioche est vide, alors on mélange les cartes de la défausse et on les transfère dans la pioche.
     * La fonction retire et renvoie la première carte de la pioche (si elle existe) et null sinon.
     * @return la carte retirée ou null
     */
    public CarteTransport piocher() {
        if (this.estVide()){
            return null;
        } else if (this.pilePioche.isEmpty()) {
            for (CarteTransport c : pileDefausse) {
                pilePioche.add(c);
                pileDefausse.remove(c);
            }
            Collections.shuffle(pilePioche);
        }
        return pilePioche.remove(0);
    }

    public void defausser(CarteTransport carte) {
        pileDefausse.add(carte);
    }

    /**
     * @return true si aucune carte ne peut être prise (si les deux piles sont vides)
     */
    public boolean estVide() {
        return pilePioche.isEmpty() && pileDefausse.isEmpty();
    }

    /**
     * Remet les cartes de la defausse dans la pioche et melange, si defausse vide, melange juste
     * @return la taille de la pioche
     * */
    public int melange(){
        if (!pileDefausse.isEmpty()){
            for (CarteTransport c: pileDefausse) {
                pilePioche.add(c);
                pileDefausse.remove(c);
            }
        }
        Collections.shuffle(pilePioche);
        return pilePioche.size();
    }

    public List<CarteTransport> getCartes() {
        ArrayList<CarteTransport> cartes = new ArrayList<>();
        cartes.addAll(pilePioche);
        cartes.addAll(pileDefausse);
        return cartes;
    }
    public int getFullSize(){return pilePioche.size() + pileDefausse.size();}

    public Map<String, Object> dataMap() {
        return Map.ofEntries(
                Map.entry("pioche", pilePioche.size()),
                Map.entry("defausse", pileDefausse));
    }
}
