package fr.umontpellier.iut.rails;

import fr.umontpellier.iut.rails.Route;
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
    private int nbPorts;
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
        this.nbPorts = 3;
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
        List<String> optionsVilles = new ArrayList<>();
        for (Ville ville : jeu.getPortsLibres()) {
            optionsVilles.add(ville.nom());
        }
        System.out.println(optionsVilles);
        List <Bouton> boutonsPioche = Arrays.asList(
                new Bouton("Piocher une carte bateau"),
                new Bouton("Piocher une carte wagon"));
        List<Bouton> boutons = Arrays.asList(
                new Bouton("Piocher dans une des piles"),
                new Bouton("Piocher une carte Destination"),
                new Bouton("Prendre Possession d'une route"),
                new Bouton("Bâtir un port"),
                new Bouton("Echanger des pions"),
                new Bouton("Prendre une carte visible"));
        String choix = choisir(
                "Que voulez vous faire ?",
                null,
                boutons,
                true);

        if (choix.equals("")) {
            log(String.format("%s ne souhaite rien faire", toLog()));
        } else {
            log(String.format("%s a choisi %s", toLog(), choix));
        }
        if (choix.equals(boutons.get(0))){
            log(String.format("%s pioche dans une des piles"));
            choisir("Que voulez vous piocher",null,boutonsPioche,true);
            if (choix.equals(boutonsPioche.get(0))){
                jeu.piocherCarteBateau();
            }
            if (choix.equals(boutonsPioche.get(1))){
                jeu.piocherCarteWagon();
            }

        }
        if (choix.equals("Prendre Possession d'une route")){
            poserRoute();
        }

        /*
        if (choix.equals(boutons.get(1))){
            Destination choisis = jeu.getPileDestinations().get(genererInt(0,jeu.getPileDestinations().size()));
            destinations.add(choisis);
           */

        }

    /* On creer une liste vide routes pour mettre toute les routes que le joueur peut choisir
     * a l'aide de boucle on verif les routes possible et on les ajoute dans la liste routes
     * on affiche la liste pour que le joueur choisit + on verif si il peut
     * si oui, on prend la route et on la retire de liste des route libre , on l'ajoute a la liste des route posseder par
     * le joueur et on update son nombre de pions wagon/bateau , on ajoute au score et on retire les carte utilisé
     * si non, on redemande au joueur de choisir
     */
    private void poserRoute() {
        List<String> routes = new ArrayList<>(); // liste vide
        for (Route route : this.jeu.getRoutesLibres()) { // ajout des routes possible
            if (this.carteEgalRoute(this.cartesTransport, route, false)) {
                if ((route.estMaritime() ? this.nbPionsBateau : this.nbPionsWagon) >= route.getLongueur()) {
                    routes.add(route.getNom());
                }
            }
        }
        log(routes.toString());
        String routeChoisis = choisir("Selectionnez une route", routes, null, false);
        log("La route choisis est : " + routeChoisis);
        boolean correspondanceTrouvee = false;
        String couleurChoisis = null;
        for (Route route : jeu.getRoutesLibres()) {
            if (Objects.equals(route.getNom(), routeChoisis)) {
                while (!correspondanceTrouvee && !carteEgalRoute(cartesTransportPosees, route, true)) {
                    for (CarteTransport carte : cartesTransport) {
                        if (Objects.equals(carte.getNom(), routeChoisis)) {
                            if (couleurChoisis == null) {
                                if (carte.getType() != TypeCarteTransport.JOKER) {
                                    couleurChoisis = carte.getCouleur().name();
                                }
                            } else if (carte.getType() != TypeCarteTransport.JOKER && !carte.getCouleur().name().equals(couleurChoisis)) {
                                cartesTransportPosees.add(carte);
                                cartesTransport.remove(carte);
                            } else {
                                log("Vous ne pouvez utilisés des cartes qui ne sont pas de la même couleurs");
                            }
                            correspondanceTrouvee = carteEgalRoute(cartesTransportPosees, route, true);
                            break;
                        }
                    }
                }
                if (correspondanceTrouvee) {
                    this.routes.add(route);
                    jeu.getRoutesLibres().remove(route);

                    if (route.estTerrestre()) {
                        nbPionsWagon -= route.getLongueur();
                    } else {
                        nbPionsBateau -= route.getLongueur();
                    }
                    for (CarteTransport carte : new ArrayList<>(cartesTransportPosees)) {
                        jeu.getPilesDeCartesBateau().defausser(carte);
                        cartesTransportPosees.remove(carte);
                    }
                    score += route.getScore();
                }
            }
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
        ArrayList<Bouton> buttons = new ArrayList<Bouton>();
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
            buttons.add(new Bouton(piochees.get(i).toString(), piochees.get(i).getNom()));
            names.add(piochees.get(i).getNom());
        }
        int nbCartesGardees = piochees.size();
        while(piochees.size() < nbMinAGarder + 1 && choixRep != ""){
            choixRep = choisir("Quelle Destination voulez vous défausser ?", names, buttons, true);
            if (choixRep != ""){
                for (Destination d :piochees) {
                    if (d.getNom() == choixRep){
                        jeu.getPileDestinations().add(d); // remet la carte defaussée au fond de la pile destination
                        piochees.remove(d);
                        break;
                    }
                }
            }
        }
        return nbCartesGardees;
    }
    /* On prends la liste de toute les routes connectés a la ville A d'une carte destination
    *  a l'aide d'une boucle for, on verifie si on retrouve bien le nom de cette ville parmi toute les villes ou le
    * joueur passe via des routes. si on passe par toute les villes on return true
    * */

    boolean destinationEstComplete(Destination d) {
        if (routesConnectees(d.getVillesDeDestination().get(0)).isEmpty()){  // si cest vide on return false
            return false;
        }

        boolean fini = false;
        List<Route> lesRoutes = routesConnectees(d.getVillesDeDestination().get(0));
        for (int i = 0; i < d.getVillesDeDestination().size(); i++) {
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
        nbPionsBateauEnReserve = 75 - nbPionsBateau;
    }

    public List<Route> getRoutes() { // get des routes que le joueurs possede
        return routes;
    }
    public List<Route> getRoutesAdjacentesDuJoueur(Ville ville) { // ce sont les routes adjacente d'une villé donné
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
}
