package DomiNations.controller;

import DomiNations.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.FileNotFoundException;
import java.util.*;

public class GameController {
    private final Game game = new Game(true,true);
    private Player[] players;
    private Kingdom[] kingdoms;
    private King[] kings ;
    private LinkedList<Domino> drawPile;
    private Bench bench;
    private int nbPlayers;
    private int nbNPC;
    private int nbKings;
    private boolean firstDrawRight;

    private ArrayList<String> names;
    private ArrayList<Integer> colors;

    public void setAll(int nbPlayers, int nbNPC, ArrayList<String> names, ArrayList<Integer> colors){
        this.nbPlayers = nbPlayers;
        this.nbNPC = nbNPC;
        this.names = names;
        this.colors = colors;
    }

    // AnchorPane principal et différents fonds
    @FXML
    private AnchorPane mainAnchorPane;
    @FXML
    private ImageView twoPlayersBackground;
    @FXML
    private ImageView threePlayersBackground;
    @FXML
    private ImageView fourPlayersBackground;

    // 3 grandes tuiles
    @FXML
    private VBox leftPanel;
    @FXML
    private VBox centerPanel;
    @FXML
    private VBox rightPanel;

    // Tuiles de chaque joueur
    @FXML
    private ArrayList<VBox> playersTiles;

    // Barres de couleurs pour chaque joueur
    @FXML
    private ArrayList<HBox> colorBars;

    // Zones de texte pour les noms des joueurs
    @FXML
    private ArrayList<Text> playerNamesTextZones;

    // Conteneur des Kings pour chaque joueur
    @FXML
    private ArrayList<VBox> kingsVbox;

    // Images des Kings (2 par joueur)
    @FXML
    private ArrayList<ArrayList<Image>> kingsImages;

    // ImageView des Kings (2 spots par joueur)
    @FXML
    private ArrayList<ArrayList<ImageView>> kingsImageViews;

    // Images des châteaux
    @FXML
    private ArrayList<Image> castleImages;

    // Images des châteaux
    @FXML
    private ArrayList<ArrayList<Image>> dominosImages;

    // ImageViews des cells des grids
    @FXML
    private ArrayList<ArrayList<ArrayList<ImageView>>> gridCells;

    // Dernière rangée du bench à enlever si 3 joueurs
    @FXML
    private VBox benchVBox;
    @FXML
    private HBox lastBenchRow;

    // ImageViews des cells du bench
    @FXML
    private ArrayList<ArrayList<ArrayList<ImageView>>> benchDominosImageViews;

    // ImageViews des kings du bench
    @FXML
    private ArrayList<ArrayList<ImageView>> benchKingsImageViews;

    // Text des numéros des dominos sur le banc
    @FXML
    private ArrayList<ArrayList<Text>> benchDominoNumbersText;

    // Boutons de déplacement
    @FXML
    private ArrayList<ArrayList<Button>> moveButtons;

    // Affichage du text
    @FXML
    private Label textPrompt;

    // Bouton pour la pioche
    @FXML
    private Button drawButton;

    @FXML
    public void initializeDisplay() {
        // Création des joueurs
        createPlayersAndKings();

        // Création des royaumes 5x5 pour chaque joueur
        initialiseKingdoms();

        // Paramètres d'affichage en fonction des joueurs (nombre de tuiles, noms et couleurs)
        setPlayersParametersOnInterface();
        removePlayerTilesAndBenchRow();

        // Création de la pioche, remplie du bon nombre de dominos et mélangée
        try {
            initialiseDrawPile();
            System.out.println("La pioche a été mélangée et contient " + drawPile.size() + " dominos.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Création et remplissage avec les premiers dominos
        bench = new Bench(nbKings);
        bench.drawFirstLane(drawPile);        // On rempli la lane de gauche pour la première fois en piochant au hasard des dominos

        chooseDominosFirstRound();
        actualiseBench();
        firstDrawRight = true;

        setButtonsEvents();
    }

    public void createPlayersAndKings() {
        String[] colorsNames = {"Rose", "Jaune", "Vert", "Bleu"};
        HashMap<String, Integer> colorsToSelect = new HashMap<>();
        for (int i=1; i<=4; i++){
            colorsToSelect.put(colorsNames[i-1],i);
        }

        this.players = new Player[nbPlayers];
        this.nbKings = (nbPlayers==3 ? 3 : 4);   // Taille 3 si 3 joueurs, taille 4 si 2 ou 4 joueurs
        this.kings = new King[nbKings];

        // Noms et couleurs des joueurs "rééls":
        for(int i = 1; i<=nbPlayers-nbNPC; i++) {
            // On crée le joueur
            Player playerInCreation = new Player();

            // On définit son nom
            playerInCreation.setName(names.get(i-1));

            // On définit la couleur
            playerInCreation.setColor(new Color(colors.get(i-1)+1));
            colorsToSelect.remove(playerInCreation.getColorName());
            players[i-1] = playerInCreation;

            // Création des rois en fonction du nombre de joueurs
            createKing(playerInCreation, i, kingsImages.get(i-1));
        }
        // Noms et couleurs des NPC
        for(int i = nbPlayers-nbNPC+1; i<=nbPlayers; i++) {
            // On crée le joueur
            NPC playerInCreation = new NPC();

            // On définit son nom (aléatoire dans la liste) et sa couleur (aléatoire dans la liste des couleurs restantes)
            playerInCreation.chooseName();
            playerInCreation.chooseColor(colorsToSelect);
            System.out.println(playerInCreation.getName() + " sera le joueur " + i + ". Il/elle jouera en " + playerInCreation.getColorName().toLowerCase() + ".");

            players[i-1] = playerInCreation;

            // Création des rois en fonction du nombre de joueurs
            createKing(playerInCreation, i, kingsImages.get(i-1));
        }

        game.setPlayers(players);
        game.setKings(kings);
    }

    // Fonction de base utilisée pour créer les kings dans createPlayersAndKings
    private void createKing(Player playerInCreation, int i, ArrayList<Image> kingsImagesOfPlayer){
        if (nbPlayers==3 || nbPlayers==4){
            King onlyKing = new King(players[i-1]);
            onlyKing.setImage(kingsImagesOfPlayer.get(0));
            kings[i-1] = onlyKing;
        }
        else if (nbPlayers == 2){
            King firstKing = new King(playerInCreation);
            firstKing.setImage(kingsImagesOfPlayer.get(0));
            kings[2*i-2] = firstKing;                           // 0 pour le joueur 1 et 3 pour le joueur 2
            King secondKing = new King(playerInCreation);
            secondKing.setImage(kingsImagesOfPlayer.get(1));
            kings[2*i-1] = secondKing;                          // 1 pour le joueur 1 et 2 pour le joueur 2
        }
    }

    private void initialiseKingdoms(){
        game.initialiseKingdoms(gridCells, castleImages); // Crée les royaumes, avec les ImageView de chaque cell et les images des châteaux
        players = game.getPlayers();
        kingdoms = game.getKingdoms();

        actualiseKingdoms(kingdoms);    // On met à jour l'affichage

        System.out.println("Les " + nbPlayers + " royaumes ont bien été créés.");
    }

    private void setPlayersParametersOnInterface(){
        // Fond
        switch (nbPlayers) {
            case 2 -> {
                threePlayersBackground.setVisible(false);
                fourPlayersBackground.setVisible(false);
            }
            case 3 -> {
                twoPlayersBackground.setVisible(false);
                fourPlayersBackground.setVisible(false);
            }
            default -> {
                twoPlayersBackground.setVisible(false);
                threePlayersBackground.setVisible(false);
            }
        }

        for (int i = 0; i < nbPlayers; i++) {
            Player player = players[i];
            Color playerColor = player.getColor();

            // Name change :
            Text playerNameTextZone = playerNamesTextZones.get(i);
            playerNameTextZone.setText(player.getName());

            // Color Bar change :
            HBox colorBar = colorBars.get(i);
            String hexValueOfPlayer = playerColor.getHexValue();
            colorBar.setStyle("-fx-background-color: "+hexValueOfPlayer);

            // Kings color change :
            ArrayList<ImageView> playerKingsImageViews = kingsImageViews.get(i);
            ArrayList<Image> kingsImagesOfPlayer = kingsImages.get(playerColor.getNumber()-1);
            for (int j=0; j<playerKingsImageViews.size(); j++){
                playerKingsImageViews.get(j).setImage(kingsImagesOfPlayer.get(j));
            }
        }
    }

    private void removePlayerTilesAndBenchRow(){
        if (nbPlayers <= 3){
            rightPanel.getChildren().remove(playersTiles.get(3));      // Si 2 ou 3 joueurs on enlève le 4ème joueur
            if (nbPlayers <= 2){
                leftPanel.getChildren().remove(playersTiles.get(2));    // Si 2 joueurs on enlève aussi le 3ème joueur
            }
            else {                                              // Si 3 on enlève le deuxième king et la dernière rangée du banc
                kingsVbox.get(0).getChildren().remove(kingsImageViews.get(0).get(1));
                kingsVbox.get(1).getChildren().remove(kingsImageViews.get(1).get(1));
                kingsVbox.get(2).getChildren().remove(kingsImageViews.get(2).get(1));
                benchVBox.getChildren().remove(lastBenchRow);
            }
        }
        else {
            kingsVbox.get(0).getChildren().remove(kingsImageViews.get(0).get(1));
            kingsVbox.get(1).getChildren().remove(kingsImageViews.get(1).get(1));
            kingsVbox.get(2).getChildren().remove(kingsImageViews.get(2).get(1));
            kingsVbox.get(3).getChildren().remove(kingsImageViews.get(3).get(1));
        }
    }

    private void actualiseKingdom(Kingdom kingdom){
        for (int x=0; x<5; x++) {
            for (int y = 0; y < 5; y++) {
                Cell cell = kingdom.getCells()[y][x];
                ImageView imageView = cell.getImageView();
                // Si la cell a une landpiece
                if (!cell.isEmpty()) {
                    LandPiece landPiece = cell.getCurrentLandPiece();
                    imageView.setVisible(true);
                    imageView.setImage(landPiece.getImage());
                } else {
                    imageView.setVisible(false);
                }
            }
        }
    }

    private void actualiseKingdoms(Kingdom[] kingdoms){
        for (int i = 0; i < nbPlayers; i++) {
            actualiseKingdom(kingdoms[i]);
        }
    }

    public void initialiseDrawPile() throws FileNotFoundException {
        game.initialiseDrawPile(nbPlayers, dominosImages);
        drawPile = game.getDrawPile();
    }

    public void actualiseBench(){
        Domino[][] twoLanes = new Domino[][]{bench.getLane(1), bench.getLane(2)};
        for (int lane=0; lane<=1; lane++){
            for (int row = 0; row < bench.getSize(); row++) {
                Domino domino = twoLanes[lane][row];
                ArrayList<ImageView> landPiecesImageViews = benchDominosImageViews.get(lane).get(row);
                ImageView sideKingImageView = benchKingsImageViews.get(lane).get(row);
                Text dominoNumberText = benchDominoNumbersText.get(lane).get(row);
                if (domino != null){
                    // On affiche le numéro du domino
                    dominoNumberText.setText(String.valueOf(domino.getNumber()));
                    // On affiche les 2 LandPieces
                    for (int i=0; i<=1; i++){
                        ImageView imageView = landPiecesImageViews.get(i);
                        imageView.setVisible(true);
                        imageView.setImage(domino.getLandPiece(i+1).getImage());
                    }
                    // On affiche le roi s'il y a
                    King king = domino.getKing();
                    if (king != null){
                        sideKingImageView.setVisible(true);
                        sideKingImageView.setImage(king.getImage());
                    }
                    else{
                        sideKingImageView.setVisible(false);
                    }
                }
                else {
                    dominoNumberText.setText("");
                    landPiecesImageViews.get(0).setVisible(false);
                    landPiecesImageViews.get(1).setVisible(false);
                }
            }
        }
    }

    public void setButtonsEvents(){

        // Pour les flèches
        for (int i=0; i<nbPlayers; i++){
            ArrayList<Button> playerButtons = moveButtons.get(i);
            Kingdom kingdom = kingdoms[i];
            for (int j=0; j<4; j++) {
                Button button = playerButtons.get(j);
                int finalJ = j;
                int finalI = i;
                button.setOnAction((event) -> {
                    try {
                        switch (finalJ) {
                            case 0 -> {
                                kingdom.move("up");
                                textPrompt.setText("Le royaume " + Integer.toString(finalI +1) + " a été déplacé vers le haut.");
                            }
                            case 1 -> {
                                kingdom.move("right");
                                textPrompt.setText("Le royaume " + Integer.toString(finalI +1) + " a été déplacé vers la droite.");
                            }
                            case 2 -> {
                                kingdom.move("down");
                                textPrompt.setText("Le royaume " + Integer.toString(finalI +1) + " a été déplacé vers le bas.");
                            }
                            case 3 -> {
                                kingdom.move("left");
                                textPrompt.setText("Le royaume " + Integer.toString(finalI +1) + " a été déplacé vers la gauche.");
                            }
                        }
                        actualiseKingdom(kingdom);
                    } catch (Exception e){
                        textPrompt.setText(e.getMessage());
                    }
                });
            }

        }

        // Pour le bouton de pioche
        drawButton.setOnAction((event) -> {
            if (drawPile.size()>0){
                if (firstDrawRight){
                    bench.drawSecondLane(drawPile);
                    actualiseBench();
                    firstDrawRight = false;
                }
                else {
                    bench.drawDominos(drawPile); // On actualise le banc en piochant des nouveaux dominos
                    actualiseBench();
                }
                textPrompt.setText(bench.getSize() + " nouveaux dominos ont été piochés.");
            }
            else{
                textPrompt.setText("La pioche est vide.");
            }
        });
    }

    public void chooseDominosFirstRound(){
        LinkedHashMap<Integer, Domino> dominosToSelect = new LinkedHashMap<>();
        LinkedList<King> kingsToPlay = new LinkedList<King>(Arrays.asList(kings));   // On récupère les rois

        // Au premier Tour l'ordre est au hasard -> mélange des rois.
        Collections.shuffle(kingsToPlay);

        // HashMap des dominos disponibles sur la lane
        int[] dominosValues = bench.getDominosValues(1);
        Domino[] lane1 = bench.getLane(1);
        for (int j=1; j<=kings.length; j++){
            dominosToSelect.put(dominosValues[j-1], lane1[j-1]);
        }

        // Pour chaque roi le joueur correspondant choisit un domino.
        for (int i=1; i<=nbKings; i++){

            actualiseBench();

            // Premier roi de la liste précédement mélangée puis le suivant à la prochaine boucle.
            King king = kingsToPlay.getFirst();
            Player currentPlayer = king.getPlayer();
            if (kingsToPlay.size()>1){
                System.out.println(currentPlayer.getName() + " " + currentPlayer.getColorEmoji() + " a été sélectionné au hasard.");
            }
            else {
                System.out.println("Il ne reste plus que " + currentPlayer.getName() + " " + currentPlayer.getColorEmoji() + ".");
            }

            // Si on a le choix entre les dominos, on demande l'user input
            if (dominosToSelect.size()>1) {
                int dominoNumber = currentPlayer.chooseDominoNumber(dominosToSelect);     // On choisit le numéro du domino (input de l'user si vrai ou choix aléatoire si NPC)
                Domino domino = dominosToSelect.get(dominoNumber);
                domino.setKing(king);                                               // On pose le King sur le domino
                king.setCurrentDomino(domino);                                      // On update le domino sur le King
                dominosToSelect.remove(dominoNumber);                               // On l'enlève des dominos à sélectionner
            }
            // S'il ne reste qu'un domino on lui attribue automatiquement
            else {
                int dominoNumber = (int) dominosToSelect.keySet().toArray()[0];     // On prend le domino restant
                Domino domino = dominosToSelect.get(dominoNumber);
                domino.setKing(king);                                               // On pose le King sur le domino
                king.setCurrentDomino(domino);                                      // On update le domino sur le King
                System.out.println("Le domino " + dominoNumber + " : " +domino.toString() + " est attribué automatiquement à " + currentPlayer.getName() + " " + currentPlayer.getColorEmoji() + ".");
                dominosToSelect.remove(dominoNumber);
            }

            kingsToPlay.removeFirst();
        }
    }
}