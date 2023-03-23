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

    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public List<Ville> getPortsLibres() {
        return new ArrayList<>(portsLibres);
    }

    public List<Route> getRoutesLibres() {
        return new ArrayList<>(routesLibres);
    }

    public List<CarteTransport> getCartesTransportVisibles() {
        return new ArrayList<>(cartesTransportVisibles);
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

        // Début du jeu
        for (Joueur j: joueurs) {
            //cartes en main
            for (int i = 0; i < 3; i++) {
                j.getCartesTransport().add(pilesDeCartesWagon.piocher());
            }
            for (int i = 0; i < 7; i++) {
                j.getCartesTransport().add(pilesDeCartesBateau.piocher());
            }
            // pions ajout
            j.setNbPionsBateau(50);
            j.setNbPionsWagon(25);
            //prendre cartes Destination
            j.prendreDestinations(true);
            // changement ratio pions
            j.setRatioPions();
            // le nombre de ports que chaque j peut poser est j.nbPorts (= à 3 au début du jeu dans constructeur)

        }
        // jeu normal
        for (Joueur j : joueurs) {
            joueurCourant = j;
            j.jouerTour();
        }
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
        return null;
    }

    public ArrayList<Route> genererFilsRoute(){
        return null;
    }



    public List<Route> getRoutesDebut() {
        return routesDebut;
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
            String choix = joueurCourant.choisir("Dans quelle pile voulez-vous piocher une carte à retourner ?", strChoixPossibles, buttons, false);
            if (choix.equals("WAGON")){
                cartesTransportVisibles.add(piocherCarteWagon());
            } else if (choix.equals("BATEAU")) { // pas besoin de verif car ne sort pas du choix si pas possible de tirer une carte du type ou pile vide
                cartesTransportVisibles.add(piocherCarteBateau());
            }
        }
    }
    
    public void poserCartesVisibles(){
        int nbW, nbB;
        if (piocheWagonEstVide() && piocheBateauEstVide())
            return;
        else if (pilesDeCartesBateau.getFullSize() + pilesDeCartesWagon.getFullSize() > 6){ // si + de 3 cartes dans chaque pioche

            if (pilesDeCartesWagon.getFullSize() == 3) { // cas ou 3 wagons et 3 sont jokers
                int cpt = 0;
                for (CarteTransport c: pilesDeCartesWagon.getPilePioche()) {
                    if(c.getCouleur().equals(Couleur.GRIS))
                        cpt++;
                }
                for (CarteTransport c: pilesDeCartesBateau.getPileDefausse()) {
                    if(c.getCouleur().equals(Couleur.GRIS))
                        cpt++;
                }
                if (cpt != 3 ){ // si pas 3 jokers c'est bon
                    nbW = 3;
                    nbB = 3;
                } else if (cpt == 3 && pilesDeCartesBateau.getFullSize() > 3){ // si on peut eviter de mettre 3 Jokers en changeant le ratio
                    nbW = 2;
                    nbB = 4;
                }else { // si plus de cartes dispo pour echanger on laisse tel quel
                    nbW = 3;
                    nbB = 3;
                }
            } else if (pilesDeCartesBateau.getFullSize() == 3 && pilesDeCartesWagon.getFullSize() >= 3) {
                // TODO : A CONTINUER
                // if ()
            }

        }
        
    }
    public PilesCartesTransport getPilesDeCartesWagon() {
        return pilesDeCartesWagon;
    }
}
