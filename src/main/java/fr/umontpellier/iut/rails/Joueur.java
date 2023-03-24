package fr.umontpellier.iut.rails;

import fr.umontpellier.iut.rails.data.*;

import java.util.*;

public class Joueur {
    public enum CouleurJouer {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    /**
     * Jeu auquel le joueur est rattaché
     */
    private final Jeu jeu;
    /**
     * Nom du joueur
     */
    private final String nom;
    /**
     * CouleurJouer du joueur (pour représentation sur le plateau)
     */
    private final CouleurJouer couleur;
    /**
     * Liste des villes sur lesquelles le joueur a construit un port
     */
    private final List<Ville> ports;
    private int nbPortsPeutPoser;
    /**
     * Liste des routes capturées par le joueur
     */
    private final List<Route> routes;
    /**
     * Nombre de pions wagons que le joueur peut encore poser sur le plateau
     */
    private int nbPionsWagon;
    /**
     * Nombre de pions wagons que le joueur a dans sa réserve (dans la boîte)
     */
    private int nbPionsWagonEnReserve;
    /**
     * Nombre de pions bateaux que le joueur peut encore poser sur le plateau
     */
    private int nbPionsBateau;
    /**
     * Nombre de pions bateaux que le joueur a dans sa réserve (dans la boîte)
     */
    private int nbPionsBateauEnReserve;
    /**
     * Liste des destinations à réaliser pendant la partie
     */
    private final List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private final List<CarteTransport> cartesTransport;
    /**
     * Liste temporaire de cartes transport que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'un port
     */
    private final List<CarteTransport> cartesTransportPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées, et points
     * perdus lors des échanges de pions)
     */
    private int score;

    public Joueur(String nom, Jeu jeu, CouleurJouer couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        this.ports = new ArrayList<>();
        this.nbPortsPeutPoser = 3;
        this.routes = new ArrayList<>();
        this.nbPionsWagon = 0;
        this.nbPionsWagonEnReserve = 25;
        this.nbPionsBateau = 0;
        this.nbPionsBateauEnReserve = 50;
        this.cartesTransport = new ArrayList<>();
        this.cartesTransportPosees = new ArrayList<>();
        this.destinations = new ArrayList<>();
        this.score = 0;
    }

    public String getNom() {
        return nom;
    }

    public List<CarteTransport> getCartesTransport(){
        return cartesTransport;
    }



    /**
     * Cette méthode est appelée à tour de rôle pour chacun des joueurs de la partie.
     * Elle doit réaliser un tour de jeu, pendant lequel le joueur a le choix entre 5 actions possibles :
     *  - piocher des cartes transport (visibles ou dans la pioche)
     *  - échanger des pions wagons ou bateau
     *  - prendre de nouvelles destinations
     *  - capturer une route
     *  - construire un port
     */
    void jouerTour() {
        // IMPORTANT : Le corps de cette fonction est à réécrire entièrement
        // Un exemple très simple est donné pour illustrer l'utilisation de certaines méthodes
        ArrayList<String> options = new ArrayList<>();
        List<Bouton> boutons = new ArrayList<>();
        int cptActions = 2;

        while (cptActions > 0){
            options.clear();
            boutons.clear();

            /*####################################################
             ################### CHOIX POSSIBLES #################
             ####################################################*/

            if (cptActions >= 1){ // Actions réalisables 2 fois par tour

                for (CarteTransport c: jeu.getCartesTransportVisibles()) {
                    if (!(cptActions == 1 && c.getType().equals(TypeCarteTransport.JOKER))){ // on prend pas les cartes J au t2
                        options.add(c.getNom());
                    }
                } // POUR CARTES VISIBLES

                if (!jeu.piocheWagonEstVide()){
                    options.add("WAGON");
                    boutons.add(new Bouton("Piocher une Carte Wagon", "WAGON"));
                } // POUR PILE CARTES TRANSPORT WAGON

                if (!jeu.piocheBateauEstVide()){
                    options.add("BATEAU");
                    boutons.add(new Bouton("Piocher une Carte Bateau", "BATEAU"));
                } // POUR PILE CARTES TRANSPORT BATEAU

            }
            if (cptActions == 2){ // Actions réalisables 1 fois par tour


                if (nbPortsPeutPoser > 0){
                    for (Ville v : jeu.getPortsLibres()) { // TODO : CHECKER SI CA MARCHE
                        if (!peutPoserPort(v.getNom()).isEmpty())
                            options.add(v.nom());
                    }
                }// POUR BATIR PORT

                for (Route r : jeu.getRoutesLibres()){ // TODO : CHECKER SI CA MARCHE
                    if (!peutPoserRoute(r.getNom(), r.getLongueur()).isEmpty()){
                        if (r.estMaritime()){
                            if (nbPionsBateau >= r.getLongueur()){
                                options.add(r.getNom());
                            }
                        } else {
                            if (nbPionsWagon >= r.getLongueur()){
                                options.add(r.getNom());
                            }
                        }
                    }
                } // POUR POSER ROUTE

                if (!jeu.getPileDestinations().isEmpty()){
                    options.add("DESTINATION");
                    boutons.add(new Bouton("Piocher une carte Destination", "DESTINATION"));
                } // POUR PIOCHER DESTINATION

                if (nbPionsWagonEnReserve >= 1 && nbPionsBateau >= 1){
                    options.add("PIONS WAGON");
                    boutons.add(new Bouton("Echanger des pions Wagon", "PIONS WAGON"));
                } // POUR ECHANGER PIONS WAGON

                if (nbPionsBateauEnReserve >= 1 && nbPionsWagon >= 1){
                    options.add("PIONS BATEAU");
                    boutons.add(new Bouton("Echanger des pions Bateau", "PIONS BATEAU"));
                } // POUR ECHANGER PIONS BATEAU



            }
           /*####################################################
             ################# FAIRE LE CHOIX ###################
             ####################################################*/

            String choix = choisir(
                    "Que voulez vous faire ?",
                    options,
                    boutons,
                    true);

            /*####################################################
             ################# APPEL DU CHOIX ####################
             ####################################################*/

            if (choix.equals("")) { // A CHOISI DE PASSER

                log(String.format("%s ne souhaite rien faire", toLog()));
                return;


            } else { // A FAIT UN CHOIX

                log(String.format("%s a choisi %s", toLog(), choix));

                if (choix.equals("WAGON")){ // ACTIONS COUTANT 1
                    this.cartesTransport.add(jeu.piocherCarteWagon());
                    cptActions -= 1;
                } else if (choix.equals("BATEAU")) {
                    this.cartesTransport.add(jeu.piocherCarteBateau());
                    cptActions -= 1;
                } else if (jeu.getCartesTransportVisibles().contains(jeu.getCarteByNom(choix))) { // si choix rpz carteVisible
                    if (jeu.getCarteByNom(choix).getType().equals(TypeCarteTransport.JOKER)){
                        piocherCarteVisible(choix);
                        cptActions = 0; // piocher un joker visible empeche de repiocher apres
                    } else {
                        cptActions -= 1;
                        piocherCarteVisible(choix);
                    }

                } else if (choix.equals("PIONS WAGON") || choix.equals("PIONS BATEAU")) { // ACTIONS COUTANT 2
                    echangerPions(choix);
                    cptActions = 0;
                } else if (choix.equals("DESTINATION")) {
                    prendreDestinations(false);
                    cptActions = 0;
                } else if (jeu.getPortsLibres().contains(jeu.getVillebyNom(choix))) { // TODO : CHECKER SI CA MARCHE
                    poserPort(choix);
                    nbPortsPeutPoser--;
                } else if (jeu.getRoutesLibres().contains(jeu.getRoutebyNom(choix))) { // TODO : CHECKER SI CA MARCHE
                    poserRoute(choix);
                    if (jeu.getRoutebyNom(choix) instanceof RouteMaritime){
                        nbPionsBateau -= jeu.getRoutebyNom(choix).getLongueur();
                    } else{
                        nbPionsWagon -= jeu.getRoutebyNom(choix).getLongueur();
                    }
                }




        /*
        if (choix.equals(boutons.get(1))){
            Destination choisis = jeu.getPileDestinations().get(genererInt(0,jeu.getPileDestinations().size()));
            destinations.add(choisis);
           */
            }
        }
    }


        private void poserPort(String nomDuPort) {
            if (this.cartesTransport.isEmpty()) {
                return;
            }
            List<Couleur> lesCouleursPaiementPossible = peutPoserPort(nomDuPort);

            List<CarteTransport> lesCartesPossedes = new ArrayList<>();
            lesCartesPossedes.addAll(cartesTransport);
            boolean peutPasser = false;
            List<String> carteTransportsEnString = new ArrayList<>();
            for (CarteTransport c : this.cartesTransport) {
                carteTransportsEnString.add(c.getNom());
            }
            int tour = 1;
            String un, deux, trois, quatre;
            int nbCartes = 0;
            int cptWagon = 0;
            int cptBateaux = 0;
            int cptJoker = 0;

            List<String> quatreCarteEnString = new ArrayList<>();
            List<CarteTransport> quatreCarte = new ArrayList<>();
            do {
                tour = 1;
                Couleur laCouleurChoisis = null;

                // premier tour qui permet de definir la couleur + choix premiere carte
                if (cptJoker + cptWagon + cptBateaux == 0 && tour == 1) {
                    do {
                        un = choisir("Veuillez selectionner deux cartes Wagons et deux cartes Bateaux", carteTransportsEnString, null, false);
                        for (CarteTransport c : lesCartesPossedes) {
                            if (un.equals(c.getNom())) {
                                quatreCarte.add(c);
                                quatreCarteEnString.add(un);
                            }
                        }
                        if (laCouleurChoisis == null) {//
                            for (CarteTransport c : lesCartesPossedes) {
                                if (un.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                    laCouleurChoisis = c.getCouleur();
                                    quatreCarte.add(c);
                                }
                            }
                        }

                    } while (quatreCarte.get(0).getAncre() == false && (lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null));

                    carteTransportsEnString.remove(un);

                }
                tour++;
                if (quatreCarte.get(0).getType() == TypeCarteTransport.WAGON) {
                    cptWagon++;
                } else if (quatreCarte.get(0).getType() == TypeCarteTransport.JOKER) {
                    cptJoker++;
                } else if (quatreCarte.get(0).getType() == TypeCarteTransport.BATEAU) {
                    cptBateaux++;
                }
                if (tour == 2) {
                    do {
                        if (quatreCarte.size() == 2) {
                            quatreCarte.remove(1);
                        }
                        if (quatreCarteEnString.size() == 2) {
                            quatreCarteEnString.remove(1);
                        }
                        deux = choisir("Veuillez selectionner une deuxieme carte", carteTransportsEnString, null, false);
                        for (CarteTransport c : lesCartesPossedes) {
                            if (deux.equals(c.getNom())) {
                                quatreCarte.add(c);
                                quatreCarteEnString.add(deux);
                            }
                        }
                        if (laCouleurChoisis == null) {//
                            for (CarteTransport c : lesCartesPossedes) {
                                if (deux.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                    laCouleurChoisis = c.getCouleur();
                                    quatreCarte.add(c);
                                }
                            }
                        }
                    } while ((quatreCarte.get(1).getAncre() == false && (!quatreCarte.get(1).getCouleur().equals(laCouleurChoisis) || laCouleurChoisis == null)) || !quatreCarte.get(1).getType().equals(TypeCarteTransport.JOKER) && (lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null));
                    carteTransportsEnString.remove(deux);
                }
                tour++;
                if (quatreCarte.get(1).getType() == TypeCarteTransport.WAGON) {
                    cptWagon++;
                } else if (quatreCarte.get(1).getType() == TypeCarteTransport.JOKER) {
                    cptJoker++;
                } else if (quatreCarte.get(1).getType() == TypeCarteTransport.BATEAU) {
                    cptBateaux++;
                }

                if (tour == 3) {
                    if (cptBateaux == 2) {
                        do {
                            if (quatreCarte.size() == 3) {
                                quatreCarte.remove(2);
                            }
                            if (quatreCarteEnString.size() == 3) {
                                quatreCarteEnString.remove(2);
                            }
                            trois = choisir("Veuillez selectionner une troisième carte", carteTransportsEnString, null, false);
                            for (CarteTransport c : lesCartesPossedes) {
                                if (trois.equals(c.getNom())) {
                                    quatreCarte.add(c);
                                    quatreCarteEnString.add(trois);
                                }
                            }
                            if (laCouleurChoisis == null) {//
                                for (CarteTransport c : lesCartesPossedes) {
                                    if (trois.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                        laCouleurChoisis = c.getCouleur();
                                    }
                                }
                            }
                        } while ((quatreCarte.get(2).getAncre() == false && (!quatreCarte.get(2).getCouleur().equals(laCouleurChoisis) || laCouleurChoisis == null)) && (quatreCarte.get(2).getType().equals(TypeCarteTransport.BATEAU) || !quatreCarte.get(2).getType().equals(TypeCarteTransport.JOKER))&& (lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null));
                    } else if (cptWagon == 2) {
                        do {
                            if (quatreCarte.size() == 3) {
                                quatreCarte.remove(2);
                            }
                            if (quatreCarteEnString.size() == 3) {
                                quatreCarteEnString.remove(2);
                            }
                            trois = choisir("Veuillez selectionner une troisième carte", carteTransportsEnString, null, false);
                            for (CarteTransport c : lesCartesPossedes) {
                                if (trois.equals(c.getNom())) {
                                    quatreCarte.add(c);
                                    quatreCarteEnString.add(trois);
                                }
                            }
                            if (laCouleurChoisis == null) {//
                                for (CarteTransport c : lesCartesPossedes) {
                                    if (trois.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                        laCouleurChoisis = c.getCouleur();
                                    }
                                }
                            }
                        } while ((quatreCarte.get(2).getAncre() == false && (!quatreCarte.get(2).getCouleur().equals(laCouleurChoisis) || laCouleurChoisis == null)) && (quatreCarte.get(2).getType().equals(TypeCarteTransport.WAGON) || !quatreCarte.get(2).getType().equals(TypeCarteTransport.JOKER)) && (lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null));

                    } else {
                        do {
                            if (quatreCarte.size() == 3) {
                                quatreCarte.remove(2);
                            }
                            if (quatreCarteEnString.size() == 3) {
                                quatreCarteEnString.remove(2);
                            }
                            trois = choisir("Veuillez selectionner une troisième carte", carteTransportsEnString, null, false);
                            for (CarteTransport c : lesCartesPossedes) {
                                if (trois.equals(c.getNom())) {
                                    quatreCarte.add(c);
                                    quatreCarteEnString.add(trois);
                                }
                            }
                            if (laCouleurChoisis == null) {//
                                for (CarteTransport c : lesCartesPossedes) {
                                    if (trois.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                        laCouleurChoisis = c.getCouleur();
                                    }
                                }
                            }
                        } while ((quatreCarte.get(2).getAncre() == false && (!quatreCarte.get(2).getCouleur().equals(laCouleurChoisis) || laCouleurChoisis == null)) || !quatreCarte.get(2).getType().equals(TypeCarteTransport.JOKER) && (lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null));
                    }
                    carteTransportsEnString.remove(trois);
                }
                tour++;
                if (quatreCarte.get(3).getType() == TypeCarteTransport.WAGON) {
                    cptWagon++;
                } else if (quatreCarte.get(3).getType() == TypeCarteTransport.JOKER) {
                    cptJoker++;
                } else if (quatreCarte.get(3).getType() == TypeCarteTransport.BATEAU) {
                    cptBateaux++;
                }
                boolean compilSolutionsImpossibles =
                        !(cptJoker >= 4)
                                && !(cptJoker >= 3 && cptWagon >= 1)
                                && !(cptJoker >= 3 && cptBateaux >= 1)
                                && !(cptJoker >= 2 && cptWagon >= 2) && !(cptJoker >= 2 && cptBateaux >= 2)
                                && !(cptJoker >= 2 && cptBateaux >= 1 && cptWagon >= 1)
                                && !(cptJoker >= 1 && cptWagon >= 2 && cptBateaux >= 1)
                                && !(cptJoker >= 1 && cptWagon >= 1 && cptBateaux >= 1)
                                && !(cptJoker == 0 && cptBateaux >= 2 && cptWagon >= 2);
                if (tour == 4) {
                    int copieDeCptJoker = cptJoker;
                    int copieDeCptWagon = cptWagon;
                    int copieDeCptBateaux = cptBateaux;

                    do {
                        cptBateaux = copieDeCptBateaux;
                        cptJoker = copieDeCptJoker;
                        cptWagon = copieDeCptWagon;
                        if (quatreCarte.size() == 4) {
                            quatreCarte.remove(3);
                        }

                        if (quatreCarteEnString.size() == 4) {
                            quatreCarteEnString.remove(3);
                        }
                        quatre = choisir("Veuillez selectionner une quatrieme carte", carteTransportsEnString, null, false);
                        for (CarteTransport c : lesCartesPossedes) {
                            if (quatre.equals(c.getNom())) {
                                quatreCarte.add(c);
                                quatreCarteEnString.add(quatre);
                            }
                        }
                        if (laCouleurChoisis == null) {//
                            for (CarteTransport c : lesCartesPossedes) {
                                if (quatre.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                    laCouleurChoisis = c.getCouleur();
                                }
                            }

                        }
                    } while  ( ( quatreCarte.get(3).getAncre() == false && ( (!quatreCarte.get(3).getCouleur().equals(laCouleurChoisis)) || laCouleurChoisis == null ) ) && compilSolutionsImpossibles && (lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null));

                }
            } while (peutPasser == false);
            for (Ville v: jeu.getPortsLibres()) {
                if (v.getNom().equals(nomDuPort)){
                    ports.add(v);
                }
            }
            for (CarteTransport c: quatreCarte) {
                if (c.getType().equals(TypeCarteTransport.BATEAU)){
                    jeu.getPilesDeCartesBateau().defausser(c);
                }
                if (c.getType().equals(TypeCarteTransport.JOKER) || c.getType().equals(TypeCarteTransport.WAGON)){
                    jeu.getPilesDeCartesWagon().defausser(c);
                }
            }
            cartesTransportPosees.addAll(quatreCarte);
        }


    private List<Couleur> peutPoserPort(String nomDuPort){
        if (this.cartesTransport.isEmpty()) {
            return new ArrayList<>();
        }
        Ville villeDuPort = null;
        for (Ville v: jeu.getPortsLibres()) {
            if (v.getNom().equals(nomDuPort)){
                 villeDuPort=v;
            }
        }
        if (!lesVillescapturésParleJoueur().contains(villeDuPort)){ // si la ville ne fait pas partie des villes capturé
            return new ArrayList<>();
        }
        if (jeu.getPortsLibres().isEmpty()){
            return new ArrayList<Couleur>();
        }
        if (!jeu.getPortsLibres().contains(villeDuPort)) { // si la ville n'est pas libre on return une liste vide
            return new ArrayList<>();
        }
        List<CarteTransport> lesCarteAncre = getCarteAncre();
        if (lesCarteAncre.size() < 4){
            return new ArrayList<Couleur>();
        }
        int cptCarteWagon = 0;
        List<CarteTransport> carteWagons = new ArrayList<>();
        int cptCarteBateau = 0;
        List<CarteTransport> carteBateau = new ArrayList<>();
        int cptCarteJoker = 0;
        List<CarteTransport> carteJoker = new ArrayList<>();
        for (int i = 0; i < lesCarteAncre.size(); i++) {
            if (lesCarteAncre.get(i).getType().equals(TypeCarteTransport.WAGON)){
                cptCarteWagon ++;
                carteWagons.add(lesCarteAncre.get(i));
            }
            else if (lesCarteAncre.get(i).getType().equals(TypeCarteTransport.BATEAU)){
                cptCarteBateau ++;
                carteBateau.add(lesCarteAncre.get(i));
            } else if (lesCarteAncre.get(i).getType().equals(TypeCarteTransport.JOKER)){
                cptCarteJoker ++;
                carteJoker.add(lesCarteAncre.get(i));
            }
        }
        boolean compilSolutionsImpossibles =
                !(cptCarteJoker >= 4)
                && !(cptCarteJoker >= 3 && cptCarteWagon >= 1)
                && !(cptCarteJoker >= 3 && cptCarteBateau >= 1)
                && !(cptCarteJoker >= 2 && cptCarteWagon >= 2) && !(cptCarteJoker >= 2 && cptCarteBateau >= 2)
                && !(cptCarteJoker >= 2 && cptCarteBateau >= 1 && cptCarteWagon >= 1)
                && !(cptCarteJoker >= 1 && cptCarteWagon >= 2 && cptCarteBateau >=1)
                && !(cptCarteJoker >= 1 && cptCarteWagon >= 1 && cptCarteBateau >=1)
                && !(cptCarteJoker == 0 && cptCarteBateau >=2 && cptCarteWagon >= 2);
        if (compilSolutionsImpossibles){
            return new ArrayList<Couleur>();
        }
        boolean paiementPossible = false;
        List<Couleur> lesCouleurs = new ArrayList<>();
        List<CarteTransport> carteAUtiliser = new ArrayList<>();
        for (Couleur c: Couleur.values()) {
            cptCarteJoker = 0;
            cptCarteBateau = 0;
            cptCarteWagon = 0;
            if (!carteAUtiliser.isEmpty()){
                carteAUtiliser.clear();
            }
            for (CarteTransport cart: lesCarteAncre) {
                if(cart.getCouleur().equals(c) && cart.getType().equals(TypeCarteTransport.WAGON) && cptCarteWagon < 2 ){
                    carteAUtiliser.add(cart);
                    cptCarteWagon++;
                }
                else if(cart.getCouleur() == c && cart.getType().equals(TypeCarteTransport.BATEAU) && cptCarteBateau< 2 ){
                    carteAUtiliser.add(cart);
                    cptCarteBateau++;
                }
                else if (cart.getType().equals(TypeCarteTransport.JOKER)){
                    carteAUtiliser.add(cart);
                    cptCarteJoker++;
                }
            }
            if (!compilSolutionsImpossibles){
                lesCouleurs.add(c);
                paiementPossible = true;
            }
        }
        if (paiementPossible == true ){
            return lesCouleurs;
        }
        else {
            return new ArrayList<Couleur>();
        }
    }

    /* On creer une liste vide routes pour mettre toute les routes que le joueur peut choisir
     * a l'aide de boucle on verif les routes possible et on les ajoute dans la liste routes
     * on affiche la liste pour que le joueur choisit + on verif si il peut
     * si oui, on prend la route et on la retire de liste des route libre , on l'ajoute a la liste des route posseder par
     * le joueur et on update son nombre de pions wagon/bateau , on ajoute au score et on retire les carte utilisé
     * si non, on redemande au joueur de choisir
     */
    private List<Couleur> peutPoserRoute(String nom,int longeur) {
        if (this.cartesTransport.isEmpty()) {
            return new ArrayList<>();
        }
        Route laRoute = null; // route surlaquel on veut poser
        for (Route r : jeu.getRoutesDebut()) { // on recupere la route
            if (r.getNom().equals(nom)) {
                laRoute = r;
            }
        }
        if (!jeu.getRoutesLibres().contains(laRoute)){ // null si la route n'est pas libre
            return null;
        }
        if (laRoute.estPair()) {
            if (laRoute.getCouleur().equals(Couleur.GRIS)) { // on peut jouer nimporte quel couleur
                List<Couleur> lesCouleurs = new ArrayList<>();
                int cptWagon = 0;
                int cptJoker = 0;
                for (Couleur c : Couleur.values()) {
                    cptWagon = 0;
                    cptJoker = 0;
                    for (CarteTransport cart : cartesTransport) {
                        if (cart.getCouleur().equals(c) && cart.getType().equals(TypeCarteTransport.WAGON)) {
                            cptWagon++;
                        } else if (cart.getType().equals(TypeCarteTransport.JOKER)) {
                            cptJoker++;
                        }

                    }
                    if (cptJoker + cptWagon >= longeur*2) {
                        lesCouleurs.add(c);
                    }
                }
                return lesCouleurs;
            }
            else { // la route n'est pas grise

                List<Couleur> lesCouleurs = new ArrayList<>();
                int cptWagon = 0;
                int cptJoker = 0;
                for (CarteTransport cart : cartesTransport) {
                    if (cart.getCouleur().equals(laRoute.getCouleur()) && cart.getType().equals(TypeCarteTransport.WAGON)) {
                        cptWagon++;
                    } else if (cart.getType().equals(TypeCarteTransport.JOKER)) {
                        cptJoker++;
                    }

                }
                if (cptJoker + cptWagon >= longeur*2) {
                    lesCouleurs.add(laRoute.getCouleur());
                }
                return lesCouleurs;
            }


        }

        if (laRoute.estMaritime()) {
            if (laRoute.getCouleur().equals(Couleur.GRIS)) { // on peut jouer nimporte quel couleur
                List<Couleur> lesCouleurs = new ArrayList<>();
                int cptJoker = 0;
                int cptBateau = 0;
                for (Couleur c : Couleur.values()) {
                    cptJoker = 0;
                    cptBateau = 0;
                    for (CarteTransport cart : cartesTransport) {
                        if (cart.getCouleur().equals(c) && cart.getType().equals(TypeCarteTransport.BATEAU) && cart.estDouble()) {
                            /*Pour payer une route maritime verte de longueur 3, il essaie de jouer :
                            carte simple puis carte double,
                            les règles précisent bien qu'on peut dépasser le coût avec des cartes double bateau, et aucune carte n'est inutile*/
                            cptBateau += 2;
                        }
                        else if (cart.getCouleur().equals(c) && cart.getType().equals(TypeCarteTransport.BATEAU) && !cart.estDouble()) {
                            cptBateau++;
                        }
                        else if (cart.getType().equals(TypeCarteTransport.JOKER)) {
                            cptJoker++;
                        }
                    }
                    if (cptJoker + cptBateau >= longeur) {
                        lesCouleurs.add(c);
                    }
                }
                return lesCouleurs;
            }
            else { // la route n'est pas grise

                List<Couleur> lesCouleurs = new ArrayList<>();
                List<CarteTransport> carteAUtilisé = new ArrayList<>();
                int cptJoker = 0;
                int cptBateau = 0;
                for (CarteTransport cart : cartesTransport) {
                    if (cart.getCouleur().equals(laRoute.getCouleur()) && cart.getType().equals(TypeCarteTransport.BATEAU) && cart.estDouble()) {
                            /*Pour payer une route maritime verte de longueur 3, il essaie de jouer :
                            carte simple puis carte double,
                            les règles précisent bien qu'on peut dépasser le coût avec des cartes double bateau, et aucune carte n'est inutile*/
                        cptBateau += 2;
                    } else if (cart.getCouleur().equals(laRoute.getCouleur()) && cart.getType().equals(TypeCarteTransport.BATEAU) && !cart.estDouble()) {
                        cptBateau++;
                    } else if (cart.getType().equals(TypeCarteTransport.JOKER)) {
                        cptJoker++;
                    }
                }
                if (cptJoker + cptBateau >= longeur) {
                    lesCouleurs.add(laRoute.getCouleur());
                }
                return lesCouleurs;
            }
        }


        if (laRoute.estTerrestre()) {
            if (laRoute.getCouleur().equals(Couleur.GRIS)) { // on peut jouer nimporte quel couleur
                List<Couleur> lesCouleurs = new ArrayList<>();
                List<CarteTransport> carteAUtilisé = new ArrayList<>();
                int cptWagon = 0;
                int cptJoker = 0;
                for (Couleur c : Couleur.values()) {
                    cptWagon = 0;
                    cptJoker = 0;
                    for (CarteTransport cart : cartesTransport) {
                        if (cart.getCouleur().equals(c) && cart.getType().equals(TypeCarteTransport.WAGON)) {
                            cptWagon++;
                        } else if (cart.getType().equals(TypeCarteTransport.JOKER)) {
                            cptJoker++;
                        }

                    }
                    if (cptJoker + cptWagon >= longeur) {
                        lesCouleurs.add(c);
                    }
                }
                return lesCouleurs;
            }
            else { // la route n'est pas grise

                List<Couleur> lesCouleurs = new ArrayList<>();
                List<CarteTransport> carteAUtilisé = new ArrayList<>();
                int cptWagon = 0;
                int cptJoker = 0;
                    for (CarteTransport cart : cartesTransport) {
                        if (cart.getCouleur().equals(laRoute.getCouleur()) && cart.getType().equals(TypeCarteTransport.WAGON)) {
                            cptWagon++;
                        } else if (cart.getType().equals(TypeCarteTransport.JOKER)) {
                            cptJoker++;
                        }
                    }
                    if (cptJoker + cptWagon >= longeur){
                        lesCouleurs.add(laRoute.getCouleur());
                    }
                return lesCouleurs;
            }
        }
        return new ArrayList<>();
    }
    private void poserRoute(String nomVille) {
        if (this.cartesTransport.isEmpty()) {
            return;
        }
        Route laRoute = null;
        for (Route r : jeu.getRoutesLibres()) {
            if (r.getNom().equals(nomVille)) {
                laRoute = r;
            }
        }
        if (peutPoserRoute(nomVille, laRoute.getLongueur()).isEmpty()) {
            return;
        }
        /* Variables utile */
        int tour = 0;
        List<Couleur> lesCouleursPaiementPossible = peutPoserRoute(nomVille, laRoute.getLongueur());
        List<CarteTransport> lesCartesPossedes = new ArrayList<>();
        lesCartesPossedes.addAll(cartesTransport);
        boolean peutPasser = false;
        List<String> carteTransportsEnString = new ArrayList<>();

        String un, deux, trois, quatre, cinq, six, sept, huit, neuf, dix;
        int nbCartes = 0;
        int cptWagon = 0;
        int cptBateaux = 0;
        int cptJoker = 0;
        List<String> lesCartesEnString = new ArrayList<>(); // les cartes du paiement en string
        List<CarteTransport> lesCartes = new ArrayList<>(); // les cartes du paiement
        boolean paiementPossible = false;
        boolean utile = false;
        int argent = 0;


        if (laRoute.estMaritime()) {
            for (CarteTransport c : this.cartesTransport) {
                if ((c.getType().equals(TypeCarteTransport.JOKER) && lesCouleursPaiementPossible.contains(c.getCouleur())) || (c.getType().equals(TypeCarteTransport.BATEAU)) && lesCouleursPaiementPossible.contains(c.getCouleur())) {
                    carteTransportsEnString.add(c.getNom());
                }
            }
            do {
                argent = 0;
                Couleur laCouleurChoisis = null;
                do {
                    utile = false;
                    un = choisir("Veuillez selectionner des cartes Wagons pour capturer la route", carteTransportsEnString, null, false);
                    for (CarteTransport c : lesCartesPossedes) {
                        if ((un.equals(c.getNom()) && (c.getType().equals(TypeCarteTransport.BATEAU)) && ((c.getCouleur().equals(laCouleurChoisis) && lesCouleursPaiementPossible.contains(laCouleurChoisis)) || laCouleurChoisis == null)) || (c.getNom().equals(nomVille) && c.getType().equals(TypeCarteTransport.JOKER))) {
                            lesCartesEnString.add(un);
                            lesCartes.add(c);
                            tour++;
                            if (c.estDouble()) {
                                argent += 2;
                            } else {
                                argent++;
                            }
                            this.cartesTransportPosees.add(c);
                            carteTransportsEnString.remove(un);
                            utile = true;
                        }
                    }
                    if (!carteTransportsEnString.isEmpty() && utile) {
                        carteTransportsEnString.remove(un);
                    }
                    if (laCouleurChoisis == null && utile) {
                        for (CarteTransport c : lesCartesPossedes) {
                            if (un.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                laCouleurChoisis = c.getCouleur();
                            }
                        }
                    }
                    if (argent >= laRoute.getLongueur() && utile) {
                        paiementPossible = true;
                    }
                } while ((lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null) && !paiementPossible);
                if (paiementPossible) {
                    peutPasser = true;
                }
            } while (peutPasser == false);

        } else if (laRoute.estTerrestre()) {
            for (CarteTransport c : this.cartesTransport) {
                if ((c.getType().equals(TypeCarteTransport.JOKER) && lesCouleursPaiementPossible.contains(c.getCouleur())) || (c.getType().equals(TypeCarteTransport.BATEAU)) && lesCouleursPaiementPossible.contains(c.getCouleur())) {
                    carteTransportsEnString.add(c.getNom());
                }
            }
            do {
                argent = 0;
                Couleur laCouleurChoisis = null;
                do {
                    utile = false;
                    un = choisir("Veuillez selectionner des cartes Wagons pour capturer la route", carteTransportsEnString, null, false);
                    for (CarteTransport c : lesCartesPossedes) {
                        if ((un.equals(c.getNom()) && (c.getType().equals(TypeCarteTransport.WAGON)) && ((c.getCouleur().equals(laCouleurChoisis) && lesCouleursPaiementPossible.contains(laCouleurChoisis)) || laCouleurChoisis == null)) || (c.getNom().equals(nomVille) && c.getType().equals(TypeCarteTransport.JOKER))) {
                            lesCartesEnString.add(un);
                            lesCartes.add(c);
                            tour++;
                            argent++;
                            this.cartesTransportPosees.add(c);
                            carteTransportsEnString.remove(un);
                            utile = true;
                        }
                    }
                    if (!carteTransportsEnString.isEmpty() && utile) {
                        carteTransportsEnString.remove(un);
                    }
                    if (laCouleurChoisis == null && utile) {
                        for (CarteTransport c : lesCartesPossedes) {
                            if (un.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                laCouleurChoisis = c.getCouleur();
                            }
                        }
                    }
                    if (argent == laRoute.getLongueur() && utile) {
                        paiementPossible = true;
                    }
                } while ((lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null) && !paiementPossible);
                if (paiementPossible) {
                    peutPasser = true;
                }
            } while (peutPasser == false);


        } else if (laRoute.estPair()) {
            for (CarteTransport c : this.cartesTransport) {
                if ((c.getType().equals(TypeCarteTransport.JOKER) && lesCouleursPaiementPossible.contains(c.getCouleur())) || (c.getType().equals(TypeCarteTransport.BATEAU)) && lesCouleursPaiementPossible.contains(c.getCouleur())) {
                    carteTransportsEnString.add(c.getNom());
                }
            }
            do {
                argent = 0;
                Couleur laCouleurChoisis = null;
                do {
                    utile = false;
                    un = choisir("Veuillez selectionner des cartes Wagons pour capturer la route", carteTransportsEnString, null, false);
                    for (CarteTransport c : lesCartesPossedes) {
                        if ((un.equals(c.getNom()) && (c.getType().equals(TypeCarteTransport.WAGON)) && ((c.getCouleur().equals(laCouleurChoisis) && lesCouleursPaiementPossible.contains(laCouleurChoisis)) || laCouleurChoisis == null)) || (c.getNom().equals(nomVille) && c.getType().equals(TypeCarteTransport.JOKER))) {
                            lesCartesEnString.add(un);
                            lesCartes.add(c);
                            tour++;
                            argent++;
                            this.cartesTransportPosees.add(c);
                            carteTransportsEnString.remove(un);
                            utile = true;
                        }
                    }
                    if (!carteTransportsEnString.isEmpty() && utile) {
                        carteTransportsEnString.remove(un);
                    }
                    if (laCouleurChoisis == null && utile) {
                        for (CarteTransport c : lesCartesPossedes) {
                            if (un.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                laCouleurChoisis = c.getCouleur();
                            }
                        }
                    }
                    if (argent == laRoute.getLongueur()*2 && utile) {
                        paiementPossible = true;
                    }
                } while ((lesCouleursPaiementPossible.contains(laCouleurChoisis) || laCouleurChoisis == null) && !paiementPossible);
                if (paiementPossible) {
                    peutPasser = true;
                }
            } while (peutPasser == false);

            this.routes.add(laRoute);
            jeu.getRoutesLibres().remove(laRoute);

            if (laRoute.estTerrestre() || laRoute.estPair()) {
                for (CarteTransport carte : lesCartes) {
                    jeu.getPilesDeCartesBateau().defausser(carte);
                }
            } else {
                for (CarteTransport carte : lesCartes) {
                    jeu.getPilesDeCartesWagon().defausser(carte);
                }
            }
            this.cartesTransportPosees.clear();
            score += laRoute.getScore();
        }
    }




    private boolean carteEgalRoute(List<CarteTransport> cartesTransportPosees, Route route, boolean montantEgal) {
        int nbCartes = cartesTransportPosees.size();
        int nbMaxCartesIdentiques = getNombreMaxCartesIdentiquesForRouteType(route.estMaritime() ? TypeCarteTransport.BATEAU : TypeCarteTransport.WAGON);
        int nbJoker = getNbJoker(cartesTransportPosees);
        int nbBonneCouleur = getNbCartesBonneCouleurEtType(cartesTransportPosees,getTypeRoute(route),route.getCouleur());
        int nbCartesNecessaires = route.getLongueur();

        if (!montantEgal && nbCartes + nbJoker + nbMaxCartesIdentiques >= nbCartesNecessaires) {
            return true;
        }

        if (montantEgal && nbCartes != nbCartesNecessaires) {
            return false;
        }

        return nbBonneCouleur + nbJoker >= nbCartesNecessaires;
    }
    private int getNombreMaxCartesIdentiquesForRouteType(TypeCarteTransport type) {
        int maxNombreCartes = 0;
        for (Route route : this.jeu.getRoutesLibres()) {
            if (getTypeRoute(route) == type && route.getLongueur() > maxNombreCartes) {
                maxNombreCartes = route.getLongueur();
            }
        }
        return maxNombreCartes;
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     *
     * Cette méthode lit les entrées du jeu (`Jeu.lireligne()`) jusqu'à ce
     * qu'un choix valide (un élément de `choix` ou de `boutons` ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     *
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     *
     * ```
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez-vous faire ceci ?", choix, null, false);
     * ```
     *
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     *
     * ```
     * List<Bouton> boutons = Arrays.asList(new Bouton("Un", "1"), new Bouton("Deux", "2"), new Bouton("Trois", "3"));
     * String input = choisir("Choisissez un nombre.", null, boutons, false);
     * ```
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de `Bouton` représentés par deux String (label,
     *                    valeur) correspondant aux choix valides attendus du joueur
     *                    qui doivent être représentés par des boutons sur
     *                    l'interface graphique (le label est affiché sur le bouton,
     *                    la valeur est ce qui est envoyé au jeu quand le bouton est
     *                    cliqué)
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élement de `choix`, ou la valeur
     * d'un élément de `boutons` ou la chaîne vide)
     */
    public String choisir(
            String instruction,
            Collection<String> choix,
            Collection<Bouton> boutons,
            boolean peutPasser) {
        if (choix == null)
            choix = new ArrayList<>();
        if (boutons == null)
            boutons = new ArrayList<>();

        HashSet<String> choixDistincts = new HashSet<>(choix);
        choixDistincts.addAll(boutons.stream().map(Bouton::valeur).toList());
        if (peutPasser || choixDistincts.isEmpty()) {
            choixDistincts.add("");
        }

        String entree;
        // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
        while (true) {
            jeu.prompt(instruction, boutons, peutPasser);
            entree = jeu.lireLigne();
            // si une réponse valide est obtenue, elle est renvoyée
            if (choixDistincts.contains(entree)) {
                return entree;
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Wagons: %d  Bateaux: %d", nbPionsWagon, nbPionsBateau));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    /**
     * @return le nombre de gardes gardées par le joueur après tirage, ou 0 si il est impossible de tirer.
     *
     * @param debut booleen indiquant si on appelle la fonction au debut de la partie, changeant le nombre de cartes a
     *              prendre et à garder obligatoirement.
     * */
    public int prendreDestinations(boolean debut){
        /* Variables temporaires pour les cartes piochees a defausser ou non*/
        ArrayList<Destination> piochees = new ArrayList<Destination>();
        int nbAPiocher, nbMinAGarder;
        /* Variables pour réaliser choix */
        ArrayList<Bouton> boutonsD = new ArrayList<Bouton>();
        ArrayList<String> names = new ArrayList<String>();
        String choixRep = " ";

        if (jeu.getPileDestinations().size() == 0)
            return 0;

        if(debut){
            nbAPiocher = 5;
            nbMinAGarder = 3;
        } else {
            if (jeu.getPileDestinations().size() < 4) {
                nbAPiocher = jeu.getPileDestinations().size();
            } else
                nbAPiocher = 4;
            nbMinAGarder = 1;
        }
        // code
        for (int i = 0; i < nbAPiocher; i++) {
            piochees.add(jeu.getPileDestinations().remove(0));
            boutonsD.add(new Bouton(piochees.get(i).toString(), piochees.get(i).getNom()));
            names.add(piochees.get(i).getNom());
        }
        int nbCartesGardees = piochees.size();
        while(piochees.size() > nbMinAGarder && !choixRep.equals("")){
            choixRep = choisir("Quelle Destination voulez vous défausser ?", names, boutonsD, true); // quelle carte defausser
            if (!choixRep.equals("")){
                for (Destination d :piochees) {
                    if (d.getNom().equals(choixRep)){
                        jeu.getPileDestinations().add(d); // remet la carte defaussée au fond de la pile destination
                        names.remove(d.getNom()); // enlever la carte défaussée de la liste des choix
                        boutonsD.removeIf(b -> b.valeur().equals(d.getNom()));
                        piochees.remove(d);
                        break;
                    }
                }
            }
        }
        for (Destination d: piochees) {
            this.destinations.add(d);
        }
        return nbCartesGardees;
    }

    boolean destinationEstComplete(Destination d) {
        if (routesConnectees(d.getVillesDeDestination().get(0)).isEmpty()){  // si cest vide on return false
            return false;
        }

        boolean fini = false;
        List<Route> lesRoutes = routesConnectees(d.getVillesDeDestination().get(0));
        for (int i = 0; i < d.getVillesDeDestination().size(); i++) {
            fini = false;
            for (int j = 0; j < lesRoutes.size(); j++) {
                if (d.getVillesDeDestination().get(i).equals(lesRoutes.get(j)) || d.getVillesDeDestination().get(i).equals(lesRoutes.get(j).getVille2())){
                    fini = true;
                }
            }
            if (fini = false){
                return false;
            }
        }
        return true;
    }

    public int calculerScoreFinal() { // a verif
        return score+ports.size()+cptScoreDestinations()+cptScorePorts();
    }

    public int cptScorePorts(){
        int cpt = 0;
        for (int i = 0; i < ports.size() ; i++) {
            int temp = 0;
            for (int j = 0; j < destinations.size(); j++) {
                if (ports.get(i).equals(destinations.get(j)) && destinationEstComplete(destinations.get(j))){
                    temp += 1;
                }
            }
            if (temp == 1){
                cpt+=20;
            }
            if (temp == 2){
                cpt += 30;
            }
            if (temp >= 3){
                cpt += 40;
            }
        }
        for (int k = 0; k < lesVillescapturésParleJoueur().size(); k++) {
            boolean present = false;
            for (int p = 0; p < ports.size(); p++) {
                if (lesVillescapturésParleJoueur().get(k).estPort() && lesVillescapturésParleJoueur().equals(ports.get(p))){
                    present = true;
                }
            }
            if (present == false){
                cpt += -4;
            }
        }
        return cpt;
    }
    public List<Ville> lesVillescapturésParleJoueur(){
        List<Ville> lesVilles = new ArrayList<>();
        for (int i = 0; i < routes.size() ; i++) {
            Route ajouter = routes.get(i);
            if (!lesVilles.contains(ajouter.getVille1())){
                lesVilles.add(ajouter.getVille1());
            }
            if (!lesVilles.contains(ajouter.getVille2())){
                lesVilles.add(ajouter.getVille2());
            }
        }
        return lesVilles;
    }
    public List<Ville> listeVilleDestinationComplete(){
        List<Ville> lesVilles = new ArrayList<>();
        for (int i = 0; i < destinations.size() ; i++) {
            if (destinationEstComplete(destinations.get(i))){
                lesVilles.addAll(destinations.get(i).getVillesDeDestination());
            }
        }
        return lesVilles;
    }


    public int cptScoreDestinations(){
        int cpt = 0; // compteur de point pour les cartes destinations
        for (int i = 0; i < this.destinations.size() ; i++) {
            if (destinationEstComplete(this.destinations.get(i))){
                if (destinations.get(i).estCarteItineraires(destinations.get(i))){  // carte itineraire
                    cpt += this.destinations.get(i).getValeurMax();
                }
                else {
                    cpt += this.destinations.get(i).getValeurSimple();
                }
            }
            if (!destinationEstComplete(this.destinations.get(i))){
                cpt += destinations.get(i).getPenalite();
            }
        }
        return cpt;
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un dictionnaire de
     * valeurs sérialisables
     * (qui sera converti en JSON pour l'envoyer à l'interface graphique)
     */
    Map<String, Object> dataMap() {
        return Map.ofEntries(
                Map.entry("nom", nom),
                Map.entry("couleur", couleur),
                Map.entry("score", score),
                Map.entry("pionsWagon", nbPionsWagon),
                Map.entry("pionsWagonReserve", nbPionsWagonEnReserve),
                Map.entry("pionsBateau", nbPionsBateau),
                Map.entry("pionsBateauReserve", nbPionsBateauEnReserve),
                Map.entry("destinationsIncompletes",
                        destinations.stream().filter(d -> !destinationEstComplete(d)).toList()),
                Map.entry("destinationsCompletes", destinations.stream().filter(this::destinationEstComplete).toList()),
                Map.entry("main", cartesTransport.stream().sorted().toList()),
                Map.entry("inPlay", cartesTransportPosees.stream().sorted().toList()),
                Map.entry("ports", ports.stream().map(Ville::nom).toList()),
                Map.entry("routes", routes.stream().map(Route::getNom).toList()));
    }
    int genererInt(int borneInf, int borneSup){
        Random random = new Random();
        int nb;
        nb = borneInf+random.nextInt(borneSup-borneInf);
        return nb;
    }

    public int getNbPionsWagon() {
        return nbPionsWagon;
    }

    public int getNbPionsWagonEnReserve() {
        return nbPionsWagonEnReserve;
    }

    public int getNbPionsBateau() {
        return nbPionsBateau;
    }

    public int getNbPionsBateauEnReserve() {
        return nbPionsBateauEnReserve;
    }

    public void setNbPionsWagon(int nbPionsWagon) {
        this.nbPionsWagon = nbPionsWagon;
    }

    public void setNbPionsWagonEnReserve(int nbPionsWagonEnReserve) {
        this.nbPionsWagonEnReserve = nbPionsWagonEnReserve;
    }

    public void setNbPionsBateau(int nbPionsBateau) {
        this.nbPionsBateau = nbPionsBateau;
    }

    public void setNbPionsBateauEnReserve(int nbPionsBateauEnReserve) {
        this.nbPionsBateauEnReserve = nbPionsBateauEnReserve;
    }
    public int getNbJoker(List<CarteTransport> cartesTransportPosees){
        int res = 0;
        for (CarteTransport carte : this.cartesTransport) {
            if (carte.getType() == TypeCarteTransport.JOKER) {
                res++;
            }
        }
        return res;
    }
    private TypeCarteTransport getTypeRoute(Route route) {
        if (route instanceof RouteMaritime) {
            return TypeCarteTransport.BATEAU;
        } else {
            return TypeCarteTransport.WAGON;
        }
    }
    private int getNbCartesBonneCouleurEtType(List<CarteTransport> cartes, TypeCarteTransport type, Couleur couleur) {
        int nbCartesBonneCouleurEtType = 0;
        for (CarteTransport carte : cartes) {
            if (carte.getCouleur() == couleur && carte.getType() == type) {
                nbCartesBonneCouleurEtType++;
            }
        }
        return nbCartesBonneCouleurEtType;
    }

    public void setRatioPions(){ // MARCHE QU'AU DEBUT
        ArrayList<String> nbWagonsAPrendre = new ArrayList<String>();
        for (int i = 10; i <= 25 ; i++) {
            nbWagonsAPrendre.add(Integer.toString(i));
        }
        log(this.nom+ " détient "+ nbPionsWagon + " pions Wagons et " + nbPionsBateau+ " pions bateaux. ");
        String choix = choisir("Choisissez le nombre de pions Wagons à prendre ", nbWagonsAPrendre, null,false );
        nbPionsWagon = Integer.parseInt(choix);
        nbPionsWagonEnReserve = 25 - nbPionsWagon;
        nbPionsBateau = 60 - nbPionsWagon;
        nbPionsBateauEnReserve = 50 - nbPionsBateau;
    }
    public List<Route> getRoutes() { // get des routes que le joueurs possede
        return routes;
    }
    public List<Route> getRoutesAdjacentesDuJoueur(Ville ville){ // ce sont les routes adjacente d'une villé donné
        List<Route> routesAdjacentes = new ArrayList<>();           // capturé par un joueur
        for (Route route : routes) {
            if (route.getVille1().equals(ville) || route.getVille2().equals(ville)) {
                routesAdjacentes.add(route);
            }
        }
        return routesAdjacentes;
    }
    public List<Route> routesConnectees(Ville depart) {
        List<Ville> villesVisitees = new ArrayList<>();
        List<Route> routesVisitees = new ArrayList<>();
        List<Ville> file = new ArrayList<>();
        villesVisitees.add(depart);
        file.add(depart);
        int i= 0;
        while (i < file.size()) {
            Ville villeCourante = file.get(i++);
            for (Route route : getRoutesAdjacentesDuJoueur(villeCourante)) {
                if (!routesVisitees.contains(route)) {
                    Ville villeSuivante = (route.getVille1().equals(villeCourante)) ? route.getVille2() : route.getVille1();

                    if (!villesVisitees.contains(villeSuivante)) {
                        villesVisitees.add(villeSuivante);
                        file.add(villeSuivante);
                    }
                    routesVisitees.add(route);
                }
            }
        }
        return routesVisitees;
    }
    public ArrayList<Route> getRouteDeVille(Ville choisis){
        ArrayList<Route> listeDeRoute = new ArrayList<>();
        for (Route r: jeu.getRoutesDebut()) {
            if (r.getVille1().equals(choisis)|| r.getVille2().equals(choisis)){
                listeDeRoute.add(r);
            }
        }
        return listeDeRoute;
    }

    public List<CarteTransport> getCarteAncre(){
        List<CarteTransport> lesCartesAncres = new ArrayList<>();
        for (CarteTransport c: cartesTransport) {
            if (c.getAncre()){
                lesCartesAncres.add(c);
            }
        }
        return lesCartesAncres;
    }

    public boolean couleurCheckCount(List<CarteTransport> listeAVerif){//check si il ya au moins 2 carte de meme couleurs
        int nbCoul = 0;
        boolean deuxCoulSiIlEstCool = false;
        for (Couleur c : Couleur.values()) {
            nbCoul = 0;
            for (CarteTransport cart : listeAVerif) {
                if (cart.getCouleur() == c){
                    nbCoul++;
                }
            }
            if (nbCoul >= 2){
                return true;
            }
        }
        return false;
    }

    public void piocherCarteVisible(String carte){
        for (CarteTransport c : jeu.getCartesTransportVisibles()) {
            if (c.getNom().equals(carte)){
                this.cartesTransport.add(c);
                jeu.poserUneCarteVisible();
                jeu.cartesTransportVisibles().remove(jeu.getCarteByNom(carte));
                return;
            }
        }
    }

    public void echangerPions(String type){
        int nbMaxAPiocher;
        ArrayList<String> nbPeutPiocher = new ArrayList<>();

        if (type.equals("PIONS WAGON")){
            if (nbPionsWagonEnReserve >= nbPionsBateau)
            nbMaxAPiocher = nbPionsBateau;
            else nbMaxAPiocher = nbPionsWagonEnReserve;

            for (int i = 1; i <= nbMaxAPiocher; i++) {
                nbPeutPiocher.add(Integer.toString(i));
            }
            String choix = choisir("Combien de pions voulez vous échanger", nbPeutPiocher, null, false);
            // On échange les points
            nbPionsWagonEnReserve -= Integer.parseInt(choix);
            nbPionsBateau -= Integer.parseInt(choix);
            nbPionsWagon += Integer.parseInt(choix);
            nbPionsBateauEnReserve += Integer.parseInt(choix);
            // On déduit du score
            score -= Integer.parseInt(choix);
        }
        else{
            if (nbPionsBateauEnReserve >= nbPionsWagon)
                nbMaxAPiocher = nbPionsWagon;
            else nbMaxAPiocher = nbPionsBateauEnReserve;

            for (int i = 1; i <= nbMaxAPiocher; i++) {
                nbPeutPiocher.add(Integer.toString(i));
            }
            String choix = choisir("Combien de pions voulez vous échanger", nbPeutPiocher, null, false);
            // On échange les points
            nbPionsWagonEnReserve += Integer.parseInt(choix);
            nbPionsBateau += Integer.parseInt(choix);
            nbPionsWagon -= Integer.parseInt(choix);
            nbPionsBateauEnReserve -= Integer.parseInt(choix);
            // On déduit du score
            score -= Integer.parseInt(choix);

        }
    }

}
