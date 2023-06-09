package fr.umontpellier.iut.rails;

import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;
import fr.umontpellier.iut.rails.data.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Jeu implements Runnable {
    /**
     * Liste des joueurs
     */
    private final List<Joueur> joueurs;
    /**
     * Le joueur dont c'est le tour
     */
    private Joueur joueurCourant;
    /**
     * Liste des villes disponibles sur le plateau de jeu
     */
    private final List<Ville> portsLibres;
    private final List<Ville> portsDebut;
    /**
     * Liste des routes disponibles sur le plateau de jeu
     */
    private final List<Route> routesLibres;
    private final List<Route> routesDebut;

    /**
     * TOUTES LES CARTES, TTES LES VILLES, TTES LES ROUTES
     * */
    private static final List<CarteTransport> allCartesTransports = new ArrayList<>();
    private final List<Ville> allVilles;

    /**
     * Pile de pioche et défausse des cartes wagon
     */
    private final PilesCartesTransport pilesDeCartesWagon;
    /**
     * Pile de pioche et défausse des cartes bateau
     */
    private final PilesCartesTransport pilesDeCartesBateau;
    /**
     * Cartes de la pioche face visible (normalement il y a 6 cartes face visible)
     */
    private final List<CarteTransport> cartesTransportVisibles;
    /**
     * Pile des cartes "Destination"
     */
    private final List<Destination> pileDestinations;
    /**
     * File d'attente des instructions recues par le serveur
     */
    private final BlockingQueue<String> inputQueue;
    /**
     * Messages d'information du jeu
     */
    private final List<String> log;

    private String instruction;
    private Collection<Bouton> boutons;

    public Jeu(String[] nomJoueurs) {
        // initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        // création des villes et des routes
        Plateau plateau = Plateau.makePlateauMonde();
        portsLibres = plateau.getPorts();
        routesLibres = plateau.getRoutes();
        this.routesDebut = new ArrayList<Route>();
        for (Route r : routesLibres) {
            this.routesDebut.add(r);
        }
        this.portsDebut = new ArrayList<>();
        for (Ville v: portsLibres) {
            this.portsDebut.add(v);
        }

        // création des piles de pioche et défausses des cartes Transport (wagon et
        // bateau)
        ArrayList<CarteTransport> cartesWagon = new ArrayList<>();
        ArrayList<CarteTransport> cartesBateau = new ArrayList<>();
        ArrayList<CarteTransport> toutesCartesTransport = new ArrayList<>();
        for (Couleur c : Couleur.values()) {
            if (c == Couleur.GRIS) {
                continue;
            }
            for (int i = 0; i < 4; i++) {
                // Cartes wagon simples avec une ancre
                cartesWagon.add(new CarteTransport(TypeCarteTransport.WAGON, c, false, true));
            }
            for (int i = 0; i < 7; i++) {
                // Cartes wagon simples sans ancre
                cartesWagon.add(new CarteTransport(TypeCarteTransport.WAGON, c, false, false));
            }
            for (int i = 0; i < 4; i++) {
                // Cartes bateau simples (toutes avec une ancre)
                cartesBateau.add(new CarteTransport(TypeCarteTransport.BATEAU, c, false, true));
            }
            for (int i = 0; i < 6; i++) {
                // Cartes bateau doubles (toutes sans ancre)
                cartesBateau.add(new CarteTransport(TypeCarteTransport.BATEAU, c, true, false));
            }
        }
        for (int i = 0; i < 14; i++) {
            // Cartes wagon joker
            cartesWagon.add(new CarteTransport(TypeCarteTransport.JOKER, Couleur.GRIS, false, true));
        }
        for (CarteTransport c: cartesBateau) {
            toutesCartesTransport.add(c);
        }
        for (CarteTransport c: cartesWagon) {
            toutesCartesTransport.add(c);
        }
        pilesDeCartesWagon = new PilesCartesTransport(cartesWagon);
        pilesDeCartesBateau = new PilesCartesTransport(cartesBateau);

        // création de la liste pile de cartes transport visibles
        // (les cartes seront retournées plus tard, au début de la partie dans run())
        cartesTransportVisibles = new ArrayList<>();

        // création des destinations
        pileDestinations = Destination.makeDestinationsMonde();
        Collections.shuffle(pileDestinations);

        // création des joueurs
        ArrayList<Joueur.CouleurJouer> couleurs = new ArrayList<>(Arrays.asList(Joueur.CouleurJouer.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();
        for (String nomJoueur : nomJoueurs) {
            joueurs.add(new Joueur(nomJoueur, this, couleurs.remove(0)));
        }
        this.joueurCourant = joueurs.get(0);

        allCartesTransports.addAll(toutesCartesTransport);

        this.allVilles = new ArrayList<>();
        this.allVilles.addAll(portsLibres);

    }

    public static CarteTransport getCarteByNom(String nom){
        for (CarteTransport c : allCartesTransports) {
            if (c.getNom().equals(nom)){
                return c;
            }
        }
        return null;
    }
    public Ville getVillebyNom(String nom){
        for (Ville v  : allVilles) {
            if (v.getNom().equals(nom))
                return v;
        }
        return null;
    }

    public Route getRoutebyNom(String nom){
        for (Route r  : routesDebut) {
            if (r.getNom().equals(nom))
                return r;
        }
        return null;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public List<Ville> getPortsLibres() {
        return new ArrayList<>(portsLibres);
    }
    public List<Ville> getVraiPortLibre(){
        return portsLibres;
    }

    public List<Route> getRoutesLibres() {
        return new ArrayList<>(routesLibres);
    }
    public List<Route> getVraiRouteLibres(){
        return this.routesLibres;
    }
    public List<CarteTransport> getCartesTransportVisibles() {
        return new ArrayList<>(cartesTransportVisibles);
    }
    public List<CarteTransport> cartesTransportVisibles() {
        return this.cartesTransportVisibles;
    }

    public List<Destination> getPileDestinations() {
        return pileDestinations;
    }

    /**
     * Exécute la partie
     *
     * C'est cette méthode qui est appelée pour démarrer la partie. Elle doit intialiser le jeu
     * (retourner les cartes transport visibles, puis demander à chaque joueur de choisir ses destinations initiales
     * et le nombre de pions wagon qu'il souhaite prendre) puis exécuter les tours des joueurs en appelant la
     * méthode Joueur.jouerTour() jusqu'à ce que la condition de fin de partie soit réalisée.
     */
    public void run() {
        // IMPORTANT : Le corps de cette fonction est à réécrire entièrement
        // Un exemple très simple est donné pour illustrer l'utilisation de certaines méthodes

        this.poserCartesVisibles(false);
        // Début du jeu
        for (Joueur j: joueurs) {
            joueurCourant = j;
            //prendre cartes Destination
            j.prendreDestinations(true);
            // changement ratio pions
            j.setRatioPions();
            // le nombre de ports que chaque j peut poser est j.nbPorts (= à 3 au début du jeu dans constructeur)

        }

        // jeu normal
        boolean finAnnoncee = false;
        while (getMinPionsJoueurs() > 6){
            for (Joueur j : joueurs) {
                joueurCourant = j;
                joueurCourant.jouerTour();
                if (finAnnoncee){
                    joueurCourant.incrementerNbToursFin();
                }
                if (joueurCourant.getSommePions() <= 6 && !finAnnoncee){
                    finAnnoncee = true;
                }
            }
        }
        // 2 TOURS FIN DE JEU
        for (int i = 0; i < 2; i++) {
            for (Joueur j : joueurs) {
                joueurCourant = j;
                if (joueurCourant.getNbToursFin() >= 2){
                    continue;
                }
                joueurCourant.jouerTour();
                if (finAnnoncee){
                    joueurCourant.incrementerNbToursFin();
                }
            }
        }

        // CALCUL DU GAGNANT
        int meilleurscore = joueurs.get(0).calculerScoreFinal() - 1;
        Joueur gagnant = null;
        for (Joueur j: joueurs) {
            if (meilleurscore < j.calculerScoreFinal()){
                meilleurscore = j.calculerScoreFinal();
                gagnant = j;
            }
        }
        log(String.format("Le joueur qui remporte la partie est %s !!!", gagnant.getNom()));
        // Fin de la partie
        prompt("Fin de la partie.", new ArrayList<>(), true);
    }


    /**
     * Pioche une carte de la pile de pioche des cartes wagon.
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CarteTransport piocherCarteWagon() {
        return pilesDeCartesWagon.piocher();
    }

    public boolean piocheWagonEstVide() {
        return pilesDeCartesWagon.estVide();
    }

    /**
     * Pioche une carte de la pile de pioche des cartes bateau.
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CarteTransport piocherCarteBateau() {
        return pilesDeCartesBateau.piocher();
    }

    public boolean piocheBateauEstVide() {
        return pilesDeCartesBateau.estVide();
    }

    /**
     * Ajoute un message au log du jeu
     */
    public void log(String message) {
        log.add(message);
    }

    /**
     * Ajoute un message à la file d'entrées
     */
    public void addInput(String message) {
        inputQueue.add(message);
    }

    /**
     * Lit une ligne de l'entrée standard
     * C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
     * l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})
     *
     * @return une chaîne de caractères correspondant à l'entrée suivante dans la
     * file
     */
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix
     *
     * @param instruction l'instruction qui est donnée au joueur
     * @param boutons     labels des choix proposés s'il y en a
     * @param peutPasser  indique si le joueur peut passer sans faire de choix
     */
    public void prompt(String instruction, Collection<Bouton> boutons, boolean peutPasser) {
        this.instruction = instruction;
        this.boutons = boutons;

        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<\n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (Bouton bouton : boutons) {
                joiner.add(bouton.toPrompt());
            }
            System.out.printf(">>> %s: %s [%s] <<<\n", joueurCourant.getNom(), instruction, joiner);
        }
        GameServer.setEtatJeu(new Gson().toJson(dataMap()));
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }

    public Map<String, Object> dataMap() {
        return Map.ofEntries(
                Map.entry("joueurs", joueurs.stream().map(Joueur::dataMap).toList()),
                Map.entry("joueurCourant", joueurs.indexOf(joueurCourant)),
                Map.entry("piocheWagon", pilesDeCartesWagon.dataMap()),
                Map.entry("piocheBateau", pilesDeCartesBateau.dataMap()),
                Map.entry("cartesTransportVisibles", cartesTransportVisibles),
                Map.entry("nbDestinations", pileDestinations.size()),
                Map.entry("instruction", instruction),
                Map.entry("boutons", boutons),
                Map.entry("log", log));
    }


    public PilesCartesTransport getPilesDeCartesBateau() {
        return this.pilesDeCartesBateau;
    }

    public ArrayList<Route> genererFilsRoute(){
        return null;
    }



    public List<Route> getRoutesDebut() {
        return routesDebut;
    }

    public int getMinPionsJoueurs(){
        int nbMinPions = joueurs.get(0).getSommePions() + 1;
        for (Joueur j: joueurs) {
            if (j.getSommePions() < nbMinPions){
                nbMinPions = j.getSommePions();
            }
        }
        return nbMinPions;
    }


    /* Fonctions cartes visibles */

    public void poserUneCarteVisible(){
        ArrayList<Bouton> buttons = new ArrayList<Bouton>();
        ArrayList<String> strChoixPossibles = new ArrayList<String>();

        if (piocheBateauEstVide() && piocheWagonEstVide())
            return;
        else{
            if (!piocheWagonEstVide()){
                buttons.add(new Bouton("WAGON"));
                strChoixPossibles.add("WAGON");
            }
            if (!piocheBateauEstVide()){
                buttons.add(new Bouton("BATEAU"));
                strChoixPossibles.add("BATEAU");
            }
            if (strChoixPossibles.size() > 0){
                String choix = joueurCourant.choisir("Dans quelle pile voulez-vous piocher une carte à retourner ?", strChoixPossibles, buttons, false);
                if (choix.equals("WAGON")){
                    cartesTransportVisibles.add(piocherCarteWagon());
                } else if (choix.equals("BATEAU")) { // pas besoin de verif car ne sort pas du choix si pas possible de tirer une carte du type ou pile vide
                    cartesTransportVisibles.add(piocherCarteBateau());
                }
            }

            verifierCartesVisibles(true);
        }
    }
    
    public void poserCartesVisibles(boolean appelApresPoserUneCarte){
        PilesCartesTransport pTempW = new PilesCartesTransport(this.pilesDeCartesWagon);
        PilesCartesTransport pTempB = new PilesCartesTransport(this.pilesDeCartesBateau);
        if (appelApresPoserUneCarte){ //on a encore la config donc on ajoute cartes dans pTemp pour que calculs marchent
            CarteTransport cAPoser;
            for (int i = 0; i < cartesTransportVisibles.size(); i++) {
                cAPoser = cartesTransportVisibles.get(0);
                if(cAPoser.getType().equals(TypeCarteTransport.WAGON) || cAPoser.getType().equals(TypeCarteTransport.JOKER)){
                    pTempW.defausser(cAPoser);
                }
                else{
                    pTempB.defausser(cAPoser);
                }
            }
        }
        int nbW = 0, nbB = 0, cpt = 0;
        boolean doitVerifier = false; // true si pas de melange perpetuel
        if (piocheWagonEstVide() && piocheBateauEstVide())
            return;
        // ON CHANGE A PARTIR DE LA
        else if (pTempW.getFullSize() >= 3) { // cdt 1

            for (CarteTransport c: pTempW.getPilePioche()) {
                if(c.getType().equals(TypeCarteTransport.JOKER))
                    cpt++;
            }
            for (CarteTransport c: pTempW.getPileDefausse()) {
                if(c.getType().equals(TypeCarteTransport.JOKER))
                    cpt++;
            } // recupere nb Jokers
            if (pTempB.getFullSize() >= 3){ // cdt 2
                if (pTempW.getFullSize() - cpt >= 6-(3-2)){ // cdt 4
                    nbW = 3;
                    nbB = 3;
                    doitVerifier = true;
                } else{
                    nbW = 3;
                    nbB = 3;
                    doitVerifier = false;
                }
            }else {
                if (pTempW.getFullSize() >= 6 - pTempB.getFullSize()){ // cdt 5
                    if (pTempW.getFullSize() - cpt >= 6- (pTempB.getFullSize() + 2)){ // cdt 6
                        nbB = pTempB.getFullSize();
                        nbW = 6 - nbB;
                        doitVerifier = true;
                    }else {
                        nbB = pTempB.getFullSize();
                        nbW = 6 - nbB;
                        doitVerifier = false;
                    }
                } else {
                    nbW = pTempW.getFullSize();
                    nbB = pTempB.getFullSize();
                    doitVerifier = false;
                }
            }
        }
        else{
            if (pTempB.getFullSize() >= 6 - pTempW.getFullSize()){ // cdt 3
                nbW = pTempW.getFullSize();
                nbB =  6 - nbW;
                doitVerifier = false;
            }else {
                nbW = pTempW.getFullSize();
                nbB = pTempB.getFullSize();
                doitVerifier = false;
            }
        }
        // FIN CHANGEMENTS
        if (appelApresPoserUneCarte && !doitVerifier){
            return;
        }

        //POSE DES CARTES
        for (int i = 0; i < nbW; i++) {
            cartesTransportVisibles.add(piocherCarteWagon());
        }
        for (int i = 0; i < nbB; i++) {
            cartesTransportVisibles.add(piocherCarteBateau());
        }
        if(doitVerifier){
            verifierCartesVisibles(false); // dans ce cas on doit verif car apres remaniement total
        }
    }

    /**
     * @return true si les cartes sont valides et false si on doit remélanger et appelle dans ce cas la pose de nouvelles
     * cartes
     * */
    public boolean verifierCartesVisibles(boolean appelApresPoserUneCarte){
        int cpt = 0;
        for (CarteTransport c: cartesTransportVisibles) {
            if (c.getType().equals(TypeCarteTransport.JOKER)){
                cpt++;
            }
        }
        if (cpt >= 3){

            if (appelApresPoserUneCarte)
                poserCartesVisibles(true);
            else {
                CarteTransport cAPoser;
                for (int i = 0; i < cartesTransportVisibles.size(); i++) {
                    cAPoser = cartesTransportVisibles.get(0);
                    if(cAPoser.getType().equals(TypeCarteTransport.WAGON) || cAPoser.getType().equals(TypeCarteTransport.JOKER)){
                        pilesDeCartesWagon.defausser(cAPoser);
                        cartesTransportVisibles.remove(cAPoser);
                    }
                    else{
                        pilesDeCartesBateau.defausser(cAPoser);
                        cartesTransportVisibles.remove(cAPoser);
                    }
                }
                poserCartesVisibles(false);}
            return false; // a rappellé la pose des cartes car c'est pas bon.
        }
        return true; // c'est bon
    }

    public ArrayList<String> getNomsCartesVisibles(){
        ArrayList<String> noms = new ArrayList<>();
        if (!cartesTransportVisibles.isEmpty()) {
            for (CarteTransport c : cartesTransportVisibles) {
                noms.add(c.getNom());
            }
        }
        return noms;
    }
    public PilesCartesTransport getPilesDeCartesWagon() {
        return pilesDeCartesWagon;
    }


}
