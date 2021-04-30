package DomiNations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

public class Game {
	private Player[] players;
	private Player currentPlayer;
	private LinkedList<Domino> drawPile;
	private int nbPlayers;

	public void play() {
		//createPlayers();
		initialiseBoards();
		try {
			initialiseDrawPile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void createPlayers() {
		int color = 0;

		Scanner scanner = new Scanner(System.in);
		System.out.println("Saisir nombre de joueur : ");
		nbPlayers= scanner.nextInt();
		scanner.nextLine();
		for(int i = 1; i<nbPlayers; i++) {
			System.out.println("Saisir nom joueur " + i + " : ");
			String name = scanner.nextLine();

			do {
				System.out.println("Saisir couleur joueur " + i + " (\"rouge\", \"bleu\", \"vert\", \"jaune\") : ");
				String color_input = scanner.nextLine();

				switch (color_input) {
					case "rouge" -> color = 1;
					case "bleu" -> color = 2;
					case "vert" -> color = 3;
					case "jaune" -> color = 4;
					default -> {
						color = 0;
						System.out.println("Saisir une couleur valide : ");
					}
				}

			} while (color == 0);

			players[i] = new Player(name, color);
		}

		currentPlayer = players[0];
	}
	
	public void initialiseBoards() {
		
	}

	public void initialiseDrawPile() throws FileNotFoundException {
		// scanner va lire le contenu du fichier .csv
		Scanner scanner = new Scanner(new File("dominos.csv"));
		scanner.nextLine();		// On saute la ligne d'en-tête

		// stringBuilder va stocker le contenu du fichier
		StringBuilder stringBuilder = new StringBuilder();
		while (scanner.hasNextLine()) {
			stringBuilder.append(scanner.nextLine())
					.append("\n");
		}
		scanner.close();

		// Les deux lignes suivantes vont ensuite produire un String[] contenant
		// les donneees du fichier CSV
		String data = stringBuilder.toString();
		String[] dominos = data.split("\n");

		// Création de la pile de dominos
		LinkedList<Domino> drawPile = new LinkedList<>();

		// Création de tous les dominos et placement dans l'ordre dans la pile drawPile
		for (int i=0; i < dominos.length; i++) {
			String[] infosDomino = dominos[i].split(",");
			LandPiece landPiece1 = new LandPiece(infosDomino[1],Integer.valueOf(infosDomino[0]));
			LandPiece landPiece2 = new LandPiece(infosDomino[3],Integer.valueOf(infosDomino[2]));
		    Domino domino = new Domino(landPiece1, landPiece2, Integer.valueOf(infosDomino[4]));
			drawPile.add(domino);
		}

		// Mélange des dominos
		Collections.shuffle(drawPile);
	}



}
