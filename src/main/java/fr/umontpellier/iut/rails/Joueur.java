package fr.umontpellier.iut.rails;

import fr.umontpellier.iut.rails.data.*;

import java.util.*;

import static fr.umontpellier.iut.rails.data.TypeCarteTransport.*;

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
     * Nombre de tours qu'a effectué le joueur
     * */
    private int nbToursFin;

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
        this.nbToursFin = 0;
        this.jeu = jeu;
        this.couleur = couleur;
        this.ports = new ArrayList<>();
        this.nbPortsPeutPoser = 3;
        this.routes = new ArrayList<>();
        this.nbPionsWagon = 25;
        this.nbPionsWagonEnReserve = 0;
        this.nbPionsBateau = 50;
        this.nbPionsBateauEnReserve = 0;
        this.cartesTransport = new ArrayList<>();
        this.cartesTransportPosees = new ArrayList<>();
        this.destinations = new ArrayList<>();
        this.score = 0;

        //cartes en main
        for (int i = 0; i < 3; i++) {
            cartesTransport.add(jeu.piocherCarteBateau());
        }
        for (int i = 0; i < 7; i++) {
            cartesTransport.add(jeu.piocherCarteBateau());
        }
    }

    public String getNom() {
        return nom;
    }

    public List<CarteTransport> getCartesTransport(){
        return cartesTransport;
    }

    public int getNbToursFin(){
        return nbToursFin;
    }

    public void incrementerNbToursFin(){
        nbToursFin++;
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
                    if (!(cptActions == 1 && c.getType().equals(JOKER))){ // on prend pas les cartes J au t2
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
                    for (Ville v : jeu.getPortsLibres()) { // TODO : CHECKER
                        if (!peutPoserPort(v.getNom()).isEmpty())
                            options.add(v.nom());
                    }
                }// POUR BATIR PORT

                for (Route r : jeu.getRoutesLibres()){ // TODO : marche pas
                    if (!peutPoserRoute(r.getNom(), r.getLongueur()).isEmpty()){
                        if (r.estMaritime()){
                            if (nbPionsBateau >= r.getLongueur()){
                                options.add(r.getNom());
                            }
                        } else {
                            // Se renseigner sur le cout d'une route pair en terme de pions (pions >= longeurx2 ?)
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

                boolean aPuJouer = false;
                for (CarteTransport c : jeu.cartesTransportVisibles()) {
                    if(c.getNom().equals(choix)){
                        if (c.getType().equals(JOKER)){
                            piocherCarteVisible(choix);
                            cptActions = 0; // piocher un joker visible empeche de repiocher apres
                        } else {
                            cptActions -= 1;
                            piocherCarteVisible(choix);
                        }
                        aPuJouer = true;
                        break;
                    }
                }
                if (aPuJouer){
                    continue;
                }
                else if (choix.equals("WAGON")){ // ACTIONS COUTANT 1
                    this.cartesTransport.add(jeu.piocherCarteWagon());
                    cptActions -= 1;
                } else if (choix.equals("BATEAU")) {
                    this.cartesTransport.add(jeu.piocherCarteBateau());
                    cptActions -= 1;
                } else if (choix.equals("PIONS WAGON") || choix.equals("PIONS BATEAU")) { // ACTIONS COUTANT 2
                    echangerPions(choix);
                    cptActions = 0;
                } else if (choix.equals("DESTINATION")) {
                    prendreDestinations(false);
                    cptActions = 0;
                } else if (jeu.getPortsLibres().contains(jeu.getVillebyNom(choix))) { // TODO : CHECKER SI CA MARCHE
                    poserPort(choix);
                    nbPortsPeutPoser--;
                    cptActions = 0;
                } else if (jeu.getRoutesLibres().contains(jeu.getRoutebyNom(choix))) { // TODO : CHECKER SI CA MARCHE
                    poserRoute(choix);
                    cptActions = 0;
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

    public CarteTransport getCarteMainByNom(String nom){
        for (CarteTransport c: cartesTransport) {
            if (c.getNom().equals(nom)){
                return c;
            }
        }
        return null;
    }

    private void poserPort(String nomDuPort){
        List<Couleur> coulPossibles = peutPoserPort(nomDuPort);
        Couleur choixCoul = null;
        int cptW = 0; int cptB = 0; int cptJ = 0;

        while(cptW + cptB + cptJ < 4){
            List<String> cartesPossibles = new ArrayList<>();
            for (CarteTransport c: this.cartesTransport) {
                if((coulPossibles.contains(c.getCouleur()) && c.isAncre()) && (choixCoul == null || choixCoul == c.getCouleur()) || c.getType().equals(JOKER)){
                    if (cptW < 2 && c.getType().equals(WAGON) || cptB < 2 && c.getType().equals(BATEAU) || c.getType().equals(JOKER)) {
                        cartesPossibles.add(c.getNom());
                    }
                }
            }
            String carteSelect = choisir("Choisissez les cartes à défausser", cartesPossibles, null, false);
            if (choixCoul == null && !getCarteMainByNom(carteSelect).getType().equals(JOKER)){
                choixCoul = getCarteMainByNom(carteSelect).getCouleur();
            }

            if (getCarteMainByNom(carteSelect).getType().equals(WAGON)){
                cptW++;
            } else if (getCarteMainByNom(carteSelect).getType().equals(BATEAU)) {
                cptB++;
            } else {
                cptJ++;
            }
            cartesTransportPosees.add(getCarteMainByNom(carteSelect));
            cartesTransport.remove(getCarteMainByNom(carteSelect));
        }
        // on defausse et met ports dans liste joueur
        CarteTransport c;
        while (!cartesTransportPosees.isEmpty()) {
            c = cartesTransportPosees.get(0);
            if (c.getType().equals(WAGON) || c.getType().equals(JOKER)){
                jeu.getPilesDeCartesWagon().defausser(c);
            } else {
                jeu.getPilesDeCartesBateau().defausser(c);
            }
            cartesTransportPosees.remove(0);
        }
        ports.add(jeu.getVillebyNom(nomDuPort));
        jeu.getVraiPortLibre().remove(jeu.getVillebyNom(nomDuPort));
    }

    private List<Couleur> peutPoserPort(String nomDuPort){
        if (this.cartesTransport.isEmpty()) {
            return new ArrayList<Couleur>();
        }
        Ville villeDuPort = null;
        for (Ville v: jeu.getPortsLibres()) {
            if (v.getNom().equals(nomDuPort)){
                 villeDuPort=v;
            }
        }
        if (!lesVillescapturésParleJoueur().contains(villeDuPort)){ // si la ville ne fait pas partie des villes capturé
            return new ArrayList<Couleur>();
        }
        if (jeu.getPortsLibres().isEmpty()){
            return new ArrayList<Couleur>();
        }
        if (!jeu.getPortsLibres().contains(villeDuPort)) { // si la ville n'est pas libre on return une liste vide
            return new ArrayList<Couleur>();
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
            if (lesCarteAncre.get(i).getType().equals(WAGON)){
                cptCarteWagon ++;
                carteWagons.add(lesCarteAncre.get(i));
            }
            else if (lesCarteAncre.get(i).getType().equals(BATEAU)){
                cptCarteBateau ++;
                carteBateau.add(lesCarteAncre.get(i));
            } else if (lesCarteAncre.get(i).getType().equals(JOKER)){
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
                if(cart.getCouleur().equals(c) && cart.getType().equals(WAGON) && cptCarteWagon < 2 ){
                    carteAUtiliser.add(cart);
                    cptCarteWagon++;
                }
                else if(cart.getCouleur() == c && cart.getType().equals(BATEAU) && cptCarteBateau< 2 ){
                    carteAUtiliser.add(cart);
                    cptCarteBateau++;
                }
                else if (cart.getType().equals(JOKER)){
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
        boolean present = false;
        Route laRoute = null; // route surlaquel on veut poser
        for (Route r : jeu.getRoutesLibres()) { // on recupere la route
            if (r.getNom().equals(nom)) {
                laRoute = r;
                present = true;
                break;
            }
        }
        if (routes.contains(laRoute.getRouteParallele())){ // Si on possede la route parallele on a pas le droit de poser cette route
            return new ArrayList<Couleur>();
        }
        if (!present){ // si laroute n'a pas ete recup dans la boucle au dessus, alors elle nest pas libre
            return new ArrayList<Couleur>();
        }

        if (laRoute.estPair()) {
            int cptPaires = 0;
            List<CarteTransport> lesJokers = new ArrayList<>();
            List<Couleur> lesCouleurs = new ArrayList<>();
            for (Couleur c : Couleur.values()) {
                int cptWagon = 0;
                for (CarteTransport cart : cartesTransport) {
                    if (cart.getCouleur().equals(c) && cart.getType().equals(TypeCarteTransport.WAGON)) {
                        cptWagon++;
                        if (cptWagon == 2){
                            cptPaires++;
                            cptWagon=0;
                            lesCouleurs.add(c);
                        }
                    } else if (cart.getType().equals(TypeCarteTransport.JOKER)) {
                        lesJokers.add(cart);
                    }
                }
                if (cptWagon == 1 && !lesJokers.isEmpty()){ // si un wagon n'a pas de pair et qu'il y'a un joker
                    cptPaires++;
                    lesCouleurs.add(c);
                    lesJokers.remove(lesJokers.size()-1);
                }
            }
            int cptJoker=0;
            if (!lesJokers.isEmpty()){ // si il reste des jokers utilisable
                for (CarteTransport c: lesJokers) {
                    cptJoker++;
                    if (cptJoker == 2){
                        lesCouleurs.add(Couleur.GRIS);
                        cptJoker=0;
                    }
                }
            }
            if (cptPaires >= longeur){
                return lesCouleurs;
            }
            return new ArrayList<Couleur>(); // pas assez de paire donc return list vide
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
                        if (cart.getCouleur().equals(c) && cart.getType().equals(BATEAU) && cart.estDouble()) {
                            /*Pour payer une route maritime verte de longueur 3, il essaie de jouer :
                            carte simple puis carte double,
                            les règles précisent bien qu'on peut dépasser le coût avec des cartes double bateau, et aucune carte n'est inutile*/
                            cptBateau += 2;
                        } else if (cart.getCouleur().equals(c) && cart.getType().equals(BATEAU) && !cart.estDouble()) {
                            cptBateau++;
                        } else if (cart.getType().equals(JOKER)) {
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
                    if (cart.getCouleur().equals(laRoute.getCouleur()) && cart.getType().equals(BATEAU) && cart.estDouble()) {
                            /*Pour payer une route maritime verte de longueur 3, il essaie de jouer :
                            carte simple puis carte double,
                            les règles précisent bien qu'on peut dépasser le coût avec des cartes double bateau, et aucune carte n'est inutile*/
                        cptBateau += 2;
                    } else if (cart.getCouleur().equals(laRoute.getCouleur()) && cart.getType().equals(BATEAU) && !cart.estDouble()) {
                        cptBateau++;
                    } else if (cart.getType().equals(JOKER)) {
                        cptJoker++;
                    }
                }
                if (cptJoker + cptBateau >= longeur) {
                    lesCouleurs.add(laRoute.getCouleur());
                }
                return lesCouleurs;
            }
        }

        if (laRoute.estTerrestre() && !laRoute.estPair()) {
            if (laRoute.getCouleur().equals(Couleur.GRIS)) { // on peut jouer nimporte quel couleur
                List<Couleur> lesCouleurs = new ArrayList<>();
                List<CarteTransport> carteAUtilisé = new ArrayList<>();
                int cptWagon = 0;
                int cptJoker = 0;
                for (Couleur c : Couleur.values()) {
                    cptWagon = 0;
                    cptJoker = 0;
                    for (CarteTransport cart : cartesTransport) {
                        if (cart.getCouleur().equals(c) && cart.getType().equals(WAGON)) {
                            cptWagon++;
                        } else if (cart.getType().equals(JOKER)) {
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
                        if (cart.getCouleur().equals(laRoute.getCouleur()) && cart.getType().equals(WAGON)) {
                            cptWagon++;
                        } else if (cart.getType().equals(JOKER)) {
                            cptJoker++;
                        }
                    }
                    if (cptJoker + cptWagon >= longeur){
                        lesCouleurs.add(laRoute.getCouleur());
                    }
                return lesCouleurs;
            }
        }
        return new ArrayList<Couleur>();
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
        /* Variables utile */
        List<Couleur> lesCouleursPaiementPossible = peutPoserRoute(nomVille, laRoute.getLongueur());
        List<CarteTransport> lesCartesPossedes = new ArrayList<>();
        List<String> carteTransportsEnString = new ArrayList<>();

        String un;
        int cptWagon = 0;
        int cptBateaux = 0;
        int cptJoker = 0;
        List<String> lesCartesEnString = new ArrayList<>(); // les cartes du paiement en string
        List<CarteTransport> lesCartes = new ArrayList<>(); // les cartes du paiement
        boolean paiementPossible = false;
        boolean utile = false;
        int argent = 0;


        if (laRoute.estMaritime()) {
            int cptSimple=0;
            int cptDouble=0;
            for (CarteTransport c : this.cartesTransport) {
                if (c.getType().equals(TypeCarteTransport.JOKER) || (c.getType().equals(TypeCarteTransport.BATEAU) && lesCouleursPaiementPossible.contains(c.getCouleur()))) {
                    lesCartesPossedes.add(c);
                    carteTransportsEnString.add(c.getNom());
                    if (c.getType().equals(JOKER) || !c.estDouble()){
                        cptSimple++;
                    }
                    else {
                        cptDouble++;
                    }
                }
            }
            if (cptDouble == laRoute.getLongueur()/2 && cptSimple ==1) {
                carteTransportsEnString.clear();
                for (CarteTransport c : this.cartesTransport) {
                    if (c.estDouble() && lesCouleursPaiementPossible.contains(c.getCouleur())) {
                        carteTransportsEnString.add(c.getNom());
                    }
                }
            }
            int utilitaire = 0;
            boolean conditionFinir = false;
            boolean carteSimple = false;
            argent = 0;
            Couleur laCouleurChoisis = null;
            do {
                if (conditionFinir==true && utilitaire == 0){
                    if (!carteTransportsEnString.isEmpty()){
                        carteTransportsEnString.clear();
                    }
                    for (CarteTransport c : lesCartesPossedes) {
                        if (c.getType().equals(JOKER)){
                            carteTransportsEnString.add(c.getNom());
                        }
                        else if(c.getType().equals(BATEAU) && !c.estDouble() && lesCouleursPaiementPossible.contains(c.getCouleur()) && (laCouleurChoisis == null || c.getCouleur().equals(laCouleurChoisis))){
                            carteTransportsEnString.add(c.getNom());
                        }
                    }
                    utilitaire++;
                }
                un = choisir("Veuillez selectionner des cartes Bateau pour capturer la route", carteTransportsEnString, null, false);
                for (CarteTransport c : lesCartesPossedes) {
                    if (un.equals(c.getNom())) {
                        if ((laCouleurChoisis == null || (c.getCouleur().equals(laCouleurChoisis) && c.getType().equals(TypeCarteTransport.BATEAU)) || c.getType().equals(TypeCarteTransport.JOKER))) {
                            if (c.estDouble()) {
                                argent += 2;
                                cptDouble--;
                            } else {
                                carteSimple = true;
                                cptSimple--;
                                argent+=1;
                            }

                            this.cartesTransportPosees.add(c);
                            this.cartesTransport.remove(c);
                            carteTransportsEnString.remove(un);
                            if (laCouleurChoisis == null) {
                                if (un.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.BATEAU)) {
                                    laCouleurChoisis = c.getCouleur();
                                }
                            }
                        }
                    }
                }

                if (argent == laRoute.getLongueur()-1 && laRoute.getLongueur()!=1){
                    if (carteSimple==true && utilitaire==0) {
                        if (cptSimple>0){
                            conditionFinir=true;
                        }
                    }
                }
                if (argent >= laRoute.getLongueur()) {
                    paiementPossible = true;
                }
                } while (!paiementPossible);
            }
        else if (laRoute.estTerrestre()) {
            for (CarteTransport c : this.cartesTransport) {
                if (c.getType().equals(TypeCarteTransport.JOKER) || (c.getType().equals(TypeCarteTransport.WAGON) && lesCouleursPaiementPossible.contains(c.getCouleur()))) {
                    lesCartesPossedes.add(c);
                    carteTransportsEnString.add(c.getNom());
                }
            }
            argent = 0;
            Couleur laCouleurChoisis = null;
            do {
                un = choisir("Veuillez selectionner des cartes Wagons pour capturer la route", carteTransportsEnString, null, false);
                for (CarteTransport c : lesCartesPossedes) {
                    if (un.equals(c.getNom())){
                        if ((laCouleurChoisis == null || (c.getCouleur().equals(laCouleurChoisis) && c.getType().equals(TypeCarteTransport.WAGON)) || c.getType().equals(TypeCarteTransport.JOKER))) {
                        this.cartesTransportPosees.add(c);
                        this.cartesTransport.remove(c);
                        carteTransportsEnString.remove(un);
                        utile = true;
                        argent++;
                            if (laCouleurChoisis == null) {
                                if (un.equals(c.getNom()) && c.getType().equals(TypeCarteTransport.JOKER) == false) {
                                    laCouleurChoisis = c.getCouleur();
                                }
                            }
                        }
                    }
                }
                if (argent == laRoute.getLongueur()) {
                    paiementPossible = true;
                }

            } while (!paiementPossible);
        }
        else if (laRoute.estPair()) {
            for (CarteTransport c : this.cartesTransport) {
                if (c.getType().equals(TypeCarteTransport.JOKER) || (c.getType().equals(TypeCarteTransport.WAGON) && lesCouleursPaiementPossible.contains(c.getCouleur()))) {
                    lesCartesPossedes.add(c);
                    carteTransportsEnString.add(c.getNom());
                }
            }
            List<Couleur> coulEncorePossible = new ArrayList<>();
            coulEncorePossible.addAll(lesCouleursPaiementPossible);
            int cptPaires = 0;

            while (!paiementPossible) {
                un = choisir("Veuillez selectionner des cartes Wagons pour capturer la route", carteTransportsEnString, null, false);
                for (CarteTransport c : lesCartesPossedes) {
                        if (un.equals(c.getNom())){
                            if ((coulEncorePossible.contains(c.getCouleur()) && c.getType().equals(TypeCarteTransport.WAGON)) || c.getType().equals(TypeCarteTransport.JOKER)) {
                                this.cartesTransportPosees.add(c);
                                this.cartesTransport.remove(c);
                                carteTransportsEnString.remove(un);
                            }
                        }
                }
                // Vérifier si le nombre de paires de cartes est suffisant pour la longueur de la route
                if (cptPaires >= laRoute.getLongueur()) {
                    paiementPossible = true;
                }
            }
        }


        if (laRoute.estTerrestre() || laRoute.estPair()) {
            for (CarteTransport carte : cartesTransportPosees) {
                jeu.getPilesDeCartesWagon().defausser(carte);
            }
        } else if (laRoute.estMaritime()){
            for (CarteTransport carte : cartesTransportPosees) {
                if (carte.getType().equals(BATEAU)){
                    jeu.getPilesDeCartesBateau().defausser(carte);
                }
                else {
                    jeu.getPilesDeCartesWagon().defausser(carte);
                }

            }
        }
        this.routes.add(laRoute);
        jeu.getVraiRouteLibres().remove(laRoute);

        this.cartesTransportPosees.clear();
        score += laRoute.getScore();
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

    boolean destinationEstComplete(Destination d){
        List<Ville> villesDestinations = d.getVillesDeDestination();
        boolean destinationComplete = true; // par défaut c'est vrai et dès que l'on trouve un ville qui n'est pas parcouru on met à false
        List<Route> groupeDeRoutes = routesConnectees(villesDestinations.get(0));

        // on parcourt toutes ville de la destinations pour voir si chacune d'entre elles se trouve dans le groupeDeRoutes
        for (Ville v : d.getVillesDeDestination()) {
            boolean estDansRoutesConnectees = false; // variable que l'on mettra à true dès que la ville sera trouvée dans groupeDeRoutes
            for (Route r : groupeDeRoutes) {
                if (r.getVille1().equals(v) || r.getVille2().equals(v)) {
                    estDansRoutesConnectees = true;
                }
            }
            if (!estDansRoutesConnectees) {
                destinationComplete = false;
                break; // break facultatif
            }
        }

        return destinationComplete;
    }

    public int calculerScoreFinal() { // a verif

        if (nbPortsPeutPoser > 0){
            int malusPortsNonPose = nbPortsPeutPoser * -4;
            return score + cptScoreDestinations() + cptScorePorts() + malusPortsNonPose;
        }
        return score + cptScoreDestinations() + cptScorePorts();

    }

    public int calculerScoreFinal2() { // a verif

        if (nbPortsPeutPoser > 0){
            int malusPortsNonPose = nbPortsPeutPoser * -4;
            return cptScoreDestinations();
        }
        return score + cptScoreDestinations() + cptScorePorts();

    }

    public int cptScorePorts(){
        int cpt = 0;
        for (int i = 0; i < ports.size() ; i++) {
            int temp = 0;
            for (int j = 0; j < destinations.size(); j++) {
                if ((destinations.get(j).getVillesDeDestination().contains(ports.get(i))) && destinationEstComplete(destinations.get(j))){
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
                    cpt += this.destinations.get(i).getValeurSimple();
                }
                else {
                    cpt += this.destinations.get(i).getValeurSimple();
                }
            }
            if (!destinationEstComplete(this.destinations.get(i))){
                cpt -= destinations.get(i).getPenalite();
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
            if (carte.getType() == JOKER) {
                res++;
            }
        }
        return res;
    }
    private TypeCarteTransport getTypeRoute(Route route) {
        if (route instanceof RouteMaritime) {
            return BATEAU;
        } else {
            return WAGON;
        }
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

    public void piocherCarteVisible(String carte){
        CarteTransport c;
        for (int i = 0; i < jeu.cartesTransportVisibles().size(); i++){
            c = jeu.cartesTransportVisibles().get(0);
            if (c.getNom().equals(carte)){
                this.cartesTransport.add(c);
                jeu.poserUneCarteVisible();
                jeu.cartesTransportVisibles().remove(c);
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
    public int getSommePions (){
        return nbPionsBateau + nbPionsWagon;
    }

    public void capturerRoutePaire(RoutePaire laRoute){
        // contientJokers
        int contientJokers = 0;
        for (CarteTransport c: cartesTransport) {
            if (c.getType().equals(JOKER)){
                contientJokers++;
                break;
            }
        }

        /*######################################################
        ############ Init points Bruts/factices ################
        ######################################################*/

        // tab Frequence
        ArrayList<Couleur> tableIndexage = new ArrayList<>();
        ArrayList<Integer> tableFrequence = new ArrayList<>();
        for (Couleur cl: Couleur.values()){
            tableIndexage.add(cl);
            tableFrequence.add(0);
        }
        for (CarteTransport c : cartesTransport) {
            for (int i = 0; i < tableIndexage.size(); i++) {
                if (c.getCouleur() == tableIndexage.get(i)){
                    tableFrequence.set(tableFrequence.get(i) + 1, i);
                }
            }
        }
        ArrayList<Integer> pointsBruts = new ArrayList<>(tableFrequence.size());
        ArrayList<Integer> pointsFactices = new ArrayList<>(tableFrequence.size());
        ArrayList<CarteTransport> cartesPossiblesBrutes = new ArrayList<>();
        ArrayList<CarteTransport> cartesPossiblesFactices = new ArrayList<>();

        /**  METTRE LE WHILE ICI ????? */
        int cpt = 0;
        for (Integer i: tableFrequence) {
            if (i >= 2){ // pointsBruts
                pointsBruts.set(cpt, i);
                // cartesPossiblesBrutes
                for (CarteTransport c : cartesTransport) {
                    if (c.getCouleur() == tableIndexage.get(cpt)){
                        cartesPossiblesBrutes.add(c);
                    }
                }
            }
            if (i % 2 == 1){ // pointsFactices
                pointsFactices.set(cpt, i % 2);
                // cartesPossibleFactices
                for (CarteTransport c : cartesTransport) {
                    if (c.getCouleur() == tableIndexage.get(cpt)) {
                        cartesPossiblesFactices.add(c);
                    }
                }
            }
            cpt++;
        }

         /*######################################################
         ################# Faire un choix  ######################
         ######################################################*/

        ArrayList<String> options = new ArrayList<>();
        for (CarteTransport c : cartesTransport) {
            if (cartesPossiblesBrutes.contains(c)){
                options.add(c.getNom());
            } else if (cartesPossiblesFactices.contains(c)) {
                options.add(c.getNom());
            }
        }
        String choix = choisir("Choisissez une carte a defausser pour payer la route", options, null, false);
        CarteTransport carteSelect = null;

         /*######################################################
         ############ Actions sur le deroulement  ###############
         ######################################################*/

        for (CarteTransport c : cartesTransport) { // recup carte choisie
            if (c.getNom().equals(choix)){
                carteSelect = c;
            }
        }

        /** CONTINUER FONCTION */
    }

}
