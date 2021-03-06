package src.findTheWumpus;
import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

/*
 *-------------------------------------Change Log-------------------------------------
 * -----01/15/2018
 * -Last minute tweaks (JC).
 * -Fixed bug when searching for torches with compass.
 * -----01/12/2018-----
 * -BUG: Compass doesn't work properly when searching for torches (BW).
 * -Commented all methods and wrote more javadoc (JC).
 * -Fixed torch exploration radius with endTurn() (JC).
 * -----01/11/2018-----
 * -Fixed random wumpus movement.
 * -DEBUG: Check endTurn() where it sets explored tiles in a radius around the player (BW).
 * -Fixed menu() when the user would complete the game (JC).
 * -Did some housekeeping stuff, annotating, etc (JC).
 * -Fixed directions for useCompass() (BW).
 * -----01/10/2018-----
 * -BUG: Wumpus doesn't move one space, it teleport to a random location
 * -Completed useCompass (BW)
 * -----01/09/2018-----
 * -Worked on making tiles near the player explored when they move, have torches, etc (JC).
 * -Got useCompass() working (BW).
 * -----01/08/2018-----
 * -Put some new stuff into useCompass (BW).
 * -IDR what I did, but I did some stuff (JC).
 * -----01/07/2018-----
 * -Added ability to have different difficulties for game boards (JC).
 * -Fixed bugs with makeBoard() and removed bias from the random spawning of items (JC).
 * -----01/06/2018-----
 * -Completely finished and debugged move() (JC).
 * -Rewrote makeBoard() so that items are generated completely randomly, but then
 * I was tweaking the code for it and now it gets stuck in an infinite loop.  I'll
 * fix that as soon as I can (JC).
 * -----01/05/2018-----
 * -BUG: There is a bias to where items are spawned with makeBoard() with the random().
 * -Did some stuff to endTurn() (BW).
 * -Started attackWumpus() (JC).
 * -Tried to fix move() cause I fucked up (JC).
 * -Started writing useCompass() (BW).
 * -----01/03/2018-----
 * -BUG: When you chose a direction with move(), it moves you in a different direction
 * than you chose.
 * -Started writing endTurn(), move(), and javadoc for state variables (JC).
 * -Started writing menu(), and displayBoard() (BW).
 * -----01/02/2018-----
 * -Started writing makeBoard() (JC).
 * -Wrote out templates for all necessary methods and state variables (BW).
 */
/**
 * This class contains the methods to create a game board using GameTile objects
 * and have the player be able to move around the board, find items, and fight
 * the wumpus.  The game board is created by initializing GameTile objects on a 
 * rectangular board in a random order, and then randomly spawning an item on those
 * game tiles.  The player has options to move across the board to different tiles,
 * display the gameboard so they can see what items are near them, use the compass
 * to give them directions on where to find a certain item, or they can choose to
 * attack the wumpus if they are close enough.  After choosing an action, the player's
 * turn ends, and the wumpus moves to a different location.  If the wumpus and the
 * player ever end up on the same game tile, the two will attack each other and the
 * player will be given a certain probability of winning.
 * <br><br>
 * This class is abstract because it does not need to be instantiated.
 * 
 * @author Joshua Ciffer, Brian Williams
 * @version 01/15/2018
 */
abstract class FindTheWumpus {

	/**
	 * Rectangular board that contains all of the characters and items for the game.
	 */
	static GameTile[][] gameBoard;

	/**
	 * Used to generate random numbers to determine spawn points, probabilities, etc.
	 */
	static Random random = new Random();

	/**
	 * Accepts user input for menu prompts.
	 */
	static Scanner userInput = new Scanner(System.in);

	/**
	 * Stores the user's response to a menu prompt.
	 */
	static String userResponse;

	/**
	 * The coordinate location of the player on the game board.
	 */
	static int playerRow, playerCol;

	/**
	 * The coordinate location of the wumpus on the game board.
	 */
	static int wumpusRow, wumpusCol;

	/**
	 * The coordinate location of the weapon on the game board.
	 */
	static int weaponRow, weaponCol;
	
	/**
	 * Keeps track of whether or not the player has picked up any items.
	 */
	static boolean weaponFound, compassFound;
	
	/**
	 * Keeps track of how many torches the player has found.
	 */
	static int torchesFound;
	
	/**
	 * Keeps track of how many torches were spawned on the game board.
	 */
	static int numTorches;
	
	/**
	 * True if the user fights the wumpus and the game ends.
	 */
	static boolean gameOver;

	/**
	 * This method creates a new GameTile[][] with the given parameters, and with
	 * all of the game items spawned in random positions. The game board will always
	 * be a rectangle, the array will never be ragged.
	 * 
	 * @param numRows - The number of rows in the game board.
	 * @param numCols - The number of columns in the game board.
	 * @param numTorches - The number of torches to be spawned.
	 * @return Returns a new GameTile[][] with the specified size, number of
	 * torches, and with all of the game items spawned.
	 */
	static GameTile[][] makeBoard(int numRows, int numCols, int numTorches) {
		GameTile[][] newBoard = new GameTile[numRows][numCols];		// Sets up empty board.
		FindTheWumpus.numTorches = numTorches;
		// Variables to keep track of what has been spawned.
		boolean playerPlaced = false, wumpusPlaced = false, weaponPlaced = false, compassPlaced = false;
		int torchesPlaced = 0;
		for (int gameTilesPlaced = 0; gameTilesPlaced < (numRows * numCols); gameTilesPlaced++) {
			int row = random.nextInt(newBoard.length);	// The coordinates of the next tiles to be created are generated randomly, and the game board
			int col = random.nextInt(newBoard[row].length);// is filled in a random order.
			if (newBoard[row][col] == null) {	// If this spot does not have a tile placed, create one.
				newBoard[row][col] = new GameTile();
				// Creates tile and randomly chooses what to spawn on it.
				while (true) {
					switch (random.nextInt(6)) {	// Randomly picks the item to spawn on the game tile.
						case 0: {	// Spawns empty tile.
							if (!(playerPlaced && wumpusPlaced && weaponPlaced && compassPlaced && (torchesPlaced == numTorches))) {
								continue;	// If the other items haven't been spawned yet, continue and spawn the items before spawning empty tiles.
							} else {
								// Leaves game tile with no items on it.
								break;	
							}
						}
						case 1: {	// Spawns player.
							if (playerPlaced) {
								continue;
							} else {
								newBoard[row][col].playerHere = true;
								newBoard[row][col].explored = true;
								playerPlaced = true;
								playerRow = row;
								playerCol = col;
								break;
							}
						}
						case 2: {	// Spawns wumpus.
							if (wumpusPlaced) {
								continue;
							} else {
								newBoard[row][col].wumpusHere = true;
								wumpusPlaced = true;
								wumpusRow = row;
								wumpusCol = col;
								break;
							}
						}
						case 3: {	// Spawns weapon.
							if (weaponPlaced) {
								continue;
							} else {
								newBoard[row][col].weaponHere = true;
								weaponPlaced = true;
								weaponRow = row;
								weaponCol = col;
								break;
							}
						}		
						case 4: {	// Spawns compass.
							if (compassPlaced) {
								continue;
							} else {
								newBoard[row][col].compassHere = true;
								compassPlaced = true;
								break;
							}
						}
						case 5: {	// Spawns torch.
							if (torchesPlaced == numTorches) {
								continue;
							} else {
								newBoard[row][col].torchHere = true;
								torchesPlaced++;
								break;
							}
						}
					}
					break;
				}
			} else {	// If a tile was already placed at this spot, do nothing.
				gameTilesPlaced--;	// The loop incrementer is decremented if a tile was already spawned at these coordinates.
			}                    // This is so the loop actually spawns the correct amount of tiles and makes sure it isn't
		}                        // counting the same tile more than once.
		return newBoard;
	}

	/**
	 * The main entry point of the program.  A board with a specified difficulty is
	 * created and then the menu is run until the game is finished.
	 * 
	 * @param args - Any command line arguments.
	 */
	public static void main(String[] args) {
		while (true) {
			// Lists options for player to select.
			System.out.print("--------Find The Wumpus Game--------\nBy Brian Williams, & Joshua Ciffer" 
					+ "\n (1) Easy - 5x5 Board, 3 Torches\n (2) Medium - 10x10 Board, 2 Torches"
					+ "\n (3) Hard - 15x15 Board, 1 Torch\n (4) Custom Difficulty\n (5) Quit\nEnter an option: ");
			// Take input.
			try {
				userResponse = userInput.next();
			} catch (InputMismatchException e) {
				System.out.println("\nPlease enter one of the given options.\n");
				userInput.next();	// Clears the scanner.
				continue;
			}
			switch (userResponse) {
				case "1": {		// Easy.
					gameBoard = makeBoard(5, 5, 3);		// 5x5 board, 3 torches.
					menu();
					break;
				}
				case "2": {		// Medium.
					gameBoard = makeBoard(10, 10, 2);	// 10x10 board, 2 torches.
					menu();
					break;
				}
				case "3": {		// Hard.
					gameBoard = makeBoard(15, 15, 1);	// 15x15 board, 1 torch.
					menu();
					break;
				}
				case "4": {		// Custom.
					// User chooses game board width, height, and number of torches.
					int rows, cols, torches;
					while (true) {
						try {
							System.out.print("How tall will the game board be?: ");
							rows = Math.abs(userInput.nextInt());	// Stores the absolute value to prevent negative array size.
						} catch (InputMismatchException e) {
							System.out.println("\nPlease enter how tall the game board will be.\n");
							userInput.next();	// Clears the scanner.
							continue;
						}
						while (true) {
							try {
								System.out.print("How wide will the game board be?: ");
								cols = Math.abs(userInput.nextInt());	// Stores the absolute value to prevent negative array size.
							} catch (InputMismatchException e) {
								System.out.println("\nPlease enter how wide the game board will be.\n");
								userInput.next();	// Clears the scanner.
								continue;
							}
							while (true) {
								try {
									System.out.print("How many torches will there be?: ");
									torches = Math.abs(userInput.nextInt());	// Stores the absolute value to prevent negative amount of torches.
								} catch (InputMismatchException e) {
									System.out.println("\nPlease enter how many torches there will be.\n");
									userInput.next();	// Clears the scanner.
									continue;
								}
								break;
							}
							break;
						}
						break;
					}
					gameBoard = makeBoard(rows, cols, torches);
					menu();
					break;
				}
				case "5": {		// Quit.
					System.exit(0);
					break;
				}
				default: {
					System.out.println("\nPlease enter one of the given options.\n");
					continue;
				}
			}
		}
	}

	/**
	 * This method displays the options available to the player and takes in their input
	 * to carry out various methods of the game.
	 */
	static void menu() {
		System.out.print("\n");
		while (true) {
			printBoard();	// Uncomment this for debugging purposes.  This prints out the board before player's turn.
			// Lists options based off of what items the player has found.
			System.out.println("Your Turn:\n (1) Display Board\n (2) Move");
			if (compassFound) {
				System.out.println(" (3) Use Compass");
			} else {
				System.out.println(" (3) LOCKED");
			}
			if ((torchesFound >= 2) && (findDistance(wumpusRow, wumpusCol) <= 2)) {
				System.out.println(" (4) Attack Wumpus");
			} else {
				System.out.println(" (4) LOCKED");
			}
			System.out.print(" (5) Quit\nEnter an Option: ");
			// Takes input.
			try {
				userResponse = userInput.next();
			} catch (InputMismatchException e) {
				System.out.println("\nPlease choose one of the given options.\n");
				userInput.next();	// Clears the scanner.
				continue;
			}
			// Selects option that player chooses.  endTurn() is called at the end of each method.
			switch (userResponse) {
				case "1": { 	// Display Board.
					displayBoard();
					break;
				}
				case "2": { 	// Move.
					move();
					break;
				}
				case "3": { 	// Use Compass.
					if (compassFound) {
						useCompass();
						break;
					} else {
						System.out.println("\nThis option is locked until you find the compass.\n");
						continue;
					}
				}
				case "4": { 	// Attack Wumpus.
					if ((torchesFound >= 2) && (findDistance(wumpusRow, wumpusCol) <= 2)) {		// Player must have 2 torches and be 2 tiles away from wumpus.
						System.out.print("\nYou attacked the wumpus!");
						if (findDistance(wumpusRow, wumpusCol) == 2) {		// If wumpus is 2 tiles away,
							if (weaponFound) {
								attackWumpus(75);	// 75% chance of winning.
							} else {
								attackWumpus(15);	// 15% chance of winning.
							}
						} else if (findDistance(wumpusRow, wumpusCol) <= 1) {	// If wumpus is 1 tile or closer,
							if (weaponFound) {
								attackWumpus(90);	// 90% chance of winning.
							} else {
								attackWumpus(30);	// 30% chance of winning.
							}
						}
					} else {
						System.out.println("\nThis option is locked until you find enough torches.\n");
						continue;
					}
					break;
				}
				case "5": { 	// Quit.
					gameOver = true;	// gameOver = true causes menu() to finish running after the switch statement.
					break;
				}
				default: {		// Error.
					System.out.println("\nEnter one of the given options.\n");
					continue;
				}
			}
			if (gameOver) {
				gameOver = false;	// Resets variable for next game.
				break;	// Goes back to main menu if game ends.
			} else {
				continue;	// Continues the game if it isn't over.
			}
		}
	}

	/**
	 * This method is run after the end of the user's turn after they have chosen
	 * an action.  If there are any items on the player's tile, they are set as found
	 * and they are removed from the tile.  If the user has moved to the tile with
	 * the wumpus on it, attackWumpus() is called with varying percentages of winning.
	 * If the player did not bump into the wumpus, the wumpus moves one tile in a random
	 * direction.  If the wumpus then bumped into the player, then attackWumpus() is
	 * called with a lower percentage of winning.  If the player did not encounter the
	 * wumpus at all, a check is performed to see if the wumpus is nearby and it notifies
	 * the player.  Finally, the tile that the player is on is set to explored, as well
	 * as any tiles in the radius of the player's torches.
	 */
	static void endTurn() {
		// Player picks up any items on their tile.
		if (gameBoard[playerRow][playerCol].weaponHere) {
			weaponFound = true;
			gameBoard[playerRow][playerCol].weaponHere = false;
			System.out.println("You found the weapon!");
		}
		if (gameBoard[playerRow][playerCol].compassHere) {
			compassFound = true;
			gameBoard[playerRow][playerCol].compassHere = false;
			System.out.println("You found the compass!");
		}
		if (gameBoard[playerRow][playerCol].torchHere) {
			torchesFound++;
			gameBoard[playerRow][playerCol].torchHere = false;
			System.out.println("You found a torch!");
		}
		// Checks to see if the user bumped into the wumpus.
		if ((playerRow == wumpusRow) && (playerCol == wumpusCol)) {		// If the player bumped into the wumpus,
			System.out.print("You bumped into the wumpus!");
			if (weaponFound) {
				attackWumpus(80);	// 80% chance of winning.
			} else {
				attackWumpus(20); 	// 20% chance of winning.
			}
		} else { 	// If the player did not bump into the wumpus, it moves to a new spot.
			while (true) {
				switch (random.nextInt(8)) {
					case 0: {	// North.
						if ((wumpusRow - 1) < 0) {		// If the wumpus would move off of the top of the board,
							continue;
						} else {
							gameBoard[wumpusRow][wumpusCol].wumpusHere = false;
							gameBoard[--wumpusRow][wumpusCol].wumpusHere = true;	// Moves wumpus one row up.
							break;
						}
					}
					case 1: {	// East.
						if ((wumpusCol + 1) > (gameBoard[wumpusRow].length - 1)) {	// If the wumpus would move off of the right of the board,
							continue;
						} else {
							gameBoard[wumpusRow][wumpusCol].wumpusHere = false;
							gameBoard[wumpusRow][++wumpusCol].wumpusHere = true;	// Moves wumpus one column right.
							break;
						}
					}
					case 2: {	// South.
						if ((wumpusRow + 1) > (gameBoard.length - 1)) {		// If the wumpus would move off of the bottom of the board,
							continue;
						} else {
							gameBoard[wumpusRow][wumpusCol].wumpusHere = false;
							gameBoard[++wumpusRow][wumpusCol].wumpusHere = true;	// Moves wumpus one row down.
							break;
						}
					}
					case 3: {	// West.
						if ((wumpusCol - 1) < 0) {		// If the wumpus would move off of the left of the board,
							continue;
						} else {
							gameBoard[wumpusRow][wumpusCol].wumpusHere = false;
							gameBoard[wumpusRow][--wumpusCol].wumpusHere = true;	// Moves wumpus one column left.
							break;
						}
					}
					case 4: {	// Northeast.
						if (((wumpusRow - 1) < 0) || ((wumpusCol + 1) > (gameBoard[wumpusRow].length - 1))) {	// If the wumpus would move off the board,
							continue;
						} else {
							gameBoard[wumpusRow][wumpusCol].wumpusHere = false;
							gameBoard[--wumpusRow][++wumpusCol].wumpusHere = true;	// Moves wumpus diagonally up, right.
							break;
						}
					}
					case 5: {	// Southeast.
						if (((wumpusRow + 1) > (gameBoard.length - 1)) || ((wumpusCol + 1) > (gameBoard[wumpusRow].length - 1))) {		// If the wumpus would move off the board,
							continue;
						} else {
							gameBoard[wumpusRow][wumpusCol].wumpusHere = false;
							gameBoard[++wumpusRow][++wumpusCol].wumpusHere = true;	// Moves wumpus diagonally down, right.
							break;
						}
					}
					case 6: {	// Southwest.
						if (((wumpusRow + 1) > (gameBoard.length - 1)) || ((wumpusCol - 1) < 0)) {	// If the wumpus would move off of the board,
							continue;
						} else {
							gameBoard[wumpusRow][wumpusCol].wumpusHere = false;
							gameBoard[++wumpusRow][--wumpusCol].wumpusHere = true;	// Moves wumpus diagonally down, left.
							break;
						}
					}
					case 7: {	// Northwest.
						if (((wumpusRow - 1) < 0) || ((wumpusCol - 1) < 0)) {	// If the wumpus would move off the board,
							continue;
						} else {
							gameBoard[wumpusRow][wumpusCol].wumpusHere = false;
							gameBoard[--wumpusRow][--wumpusCol].wumpusHere = true;	// Moves wumpus diagonally up, left.
							break;
						}
					}
				}
				break;
			}
			if ((playerRow == wumpusRow) && (playerCol == wumpusCol)) {		// If the wumpus bumped into the player,
				System.out.print("The wumpus bumped into you!");
				if (weaponFound) {
					attackWumpus(65);	// 65% chance of winning.
				} else {
					attackWumpus(5); 	// 5% chance of winning.
				}
			} else if (findDistance(wumpusRow, wumpusCol) <= torchesFound) {	// If the wumpus is close by,
				System.out.println("You have found wumpus droppings. A wumpus must be near by.");
			}
		}
		// Sets explored tiles.
		gameBoard[playerRow][playerCol].explored = true;	// Tile player is currently on.
		for (int i = 1; i <= torchesFound; i++) {	// Tiles in the radius of the player depending on the number of torches they found.
			if ((playerRow - i) >= 0) {
				gameBoard[playerRow - i][playerCol].explored = true;	// Tile to the North of the player.
			} 
			if ((playerCol + i) <= (gameBoard[playerRow].length - 1)) {
				gameBoard[playerRow][playerCol + i].explored = true;	// Tile to the East of the player.
			}
			if ((playerRow + i) <= (gameBoard.length - 1)) {
				gameBoard[playerRow + i][playerCol].explored = true;	// Tile to the South of the player.
			}
			if ((playerCol - i) >= 0) {
				gameBoard[playerRow][playerCol - i].explored = true;	// Tile to the West of the player.
			}
			if (((playerRow - i) >= 0) && ((playerCol + i) <= (gameBoard[playerRow].length - 1))) {
				gameBoard[playerRow - i][playerCol + i].explored = true;	// Tile to the Northeast of the player.
			}
			if (((playerRow + i) <= (gameBoard.length - 1)) && ((playerCol + i) <= (gameBoard[playerRow].length - 1))) {
				gameBoard[playerRow + i][playerCol + i].explored = true;	// Tile to the Southeast of the player.
			}
			if (((playerRow + i) <= (gameBoard.length - 1)) && ((playerCol - i) >= 0)) {
				gameBoard[playerRow + i][playerCol - i].explored = true;	// Tile to the Southwest of the player.
			}
			if (((playerRow - i) >= 0) && ((playerCol - i) >= 0)) {
				gameBoard[playerRow - i][playerCol - i].explored = true;	// Tile to the Northwest of the player.
			}
		}
		System.out.print("\n");
	}

	/**
	 * This method shows the player the gameboard but only reveals tiles they have explored.
	 */
	static void displayBoard() {
		for (int row = 0; row < gameBoard.length; row++) {
			for (int col = 0; col < gameBoard[row].length; col++) {
				if (gameBoard[row][col].playerHere) {
					System.out.print("P\t");	// Player at this tile.
				} else if (gameBoard[row][col].explored) {
					if (gameBoard[row][col].wumpusHere) {
						System.out.print("W\t");	// Wumpus at this tile.
					} else if (gameBoard[row][col].weaponHere) {
						System.out.print("A\t");	// Weapon at this tile.
					} else if (gameBoard[row][col].compassHere) {
						System.out.print("C\t");	// Compass at this tile.
					} else if (gameBoard[row][col].torchHere) {
						System.out.print("T\t");	// Torch at this tile.
					} else if (gameBoard[row][col].explored) {
						System.out.print("O\t");	// Explored tile.
					} else {
						System.out.print("X\t");	// Unexplored tile.
					}
				} else {
					System.out.print("X\t");	// Unexplored tile.
				}
			}
			System.out.print("\n");
		}
		endTurn();
	}
	
	/**
	 * This method prints out the entire contents of the game board for debugging purposes.
	 */
	static void printBoard() {
		for (int row = 0; row < gameBoard.length; row++) {
			for (int col = 0; col < gameBoard[row].length; col++) {
				if (gameBoard[row][col].playerHere) {
					System.out.print("P\t");	// Player at this tile.
				} else if (gameBoard[row][col].wumpusHere) {
					System.out.print("W\t");	// Wumpus at this tile.
				} else if (gameBoard[row][col].weaponHere) {
					System.out.print("A\t");	// Weapon at this tile.
				} else if (gameBoard[row][col].compassHere) {
					System.out.print("C\t");	// Compass at this tile.
				} else if (gameBoard[row][col].torchHere) {
					System.out.print("T\t");	// Torch at this tile.
				} else if (gameBoard[row][col].explored) {
					System.out.print("O\t");	// Explored tile.
				} else {
					System.out.print("X\t");	// Unexplored tile.
				}
			}
			System.out.print("\n");
		}
	}
	
	/**
	 * This method prompts the user to move in a North, East, South, West, Northeast,
	 * Southeast, Southwest, or Northwest direction. The user can choose to cancel and 
	 * go back to the menu. If the user is at the edge of the board, they are prompted 
	 * with a message telling them that they can't move in that direction. Once the 
	 * player moves, the turn ends and endTurn() is called.
	 */
	static void move() {
		System.out.print("\n");
		while (true) {
			// User chooses direction.
			System.out.print("Do you want to move to the North, East, South, West,\n"
					+ "Northeast, Southeast, Southwest, Northwest, or Cancel?: ");
			// Takes input.
			try {
				userResponse = userInput.next();
			} catch (InputMismatchException e) {
				System.out.println("\nPlease choose a direction to move.\n");
				userInput.next(); 	// Clears the Scanner.
				continue;
			}
			switch (userResponse.toLowerCase()) {
				case "north": {
					if ((playerRow - 1) < 0) {  	// If the player would move off of the top of the board,
						System.out.println("\nUh oh, it looks like you can't move to the North. Try a different direction.");
						continue;
					} else {
						gameBoard[playerRow][playerCol].playerHere = false;
						gameBoard[--playerRow][playerCol].playerHere = true;	// Moves player one row up.
						System.out.println("\nYou moved to the North.\n");
						endTurn();
						break;
					}
				}
				case "east": {
					if ((playerCol + 1) > (gameBoard[playerRow].length - 1)) {	  // If the user would move off of the right of the board,
						System.out.println("\nUh oh, it looks like you can't move to the East. Try a different direction.");
						continue;
					} else {
						gameBoard[playerRow][playerCol].playerHere = false;
						gameBoard[playerRow][++playerCol].playerHere = true;	// Moves player one column right.
						System.out.println("\nYou moved to the East.\n");
						endTurn();
						break;
					}
				}
				case "south": {
					if ((playerRow + 1) > (gameBoard.length - 1)) {	  // If the user would move off of the bottom of the board,
						System.out.println("\nUh oh, it looks like you can't move to the South. Try a different direction.");
						continue;
					} else {
						gameBoard[playerRow][playerCol].playerHere = false;
						gameBoard[++playerRow][playerCol].playerHere = true;	// Moves player one row down.
						System.out.println("\nYou moved to the South.\n");
						endTurn();
						break;
					}
				}
				case "west": {
					if ((playerCol - 1) < 0) {  	// If the user would move off of the left of the board,
						System.out.println("\nUh oh, it looks like you can't move to the West. Try a different direction.");
						continue;
					} else {
						gameBoard[playerRow][playerCol].playerHere = false;
						gameBoard[playerRow][--playerCol].playerHere = true;	// Moves player one column left.
						System.out.println("\nYou moved to the West.\n");
						endTurn();
						break;
					}
				}
				case "northeast": {
					if (((playerRow - 1) < 0) || ((playerCol + 1) > (gameBoard[playerRow].length - 1))) {	// If the user would move off the board,
						System.out.println("\nUh oh, it looks like you can't move to the North East");
						continue;
					} else {
						gameBoard[playerRow][playerCol].playerHere = false;
						gameBoard[--playerRow][++playerCol].playerHere = true; 	// Moves player diagonally up, right.
						System.out.println("\nYou moved to the North East.\n");
						endTurn();
						break;
					}
				}
				case "southeast": {
					if (((playerRow + 1) > (gameBoard.length - 1)) || ((playerCol + 1) > (gameBoard[playerRow].length - 1))) {	 // If the user would move off the board,
						System.out.println("\nUh oh, it looks like you can't move to the South East.\n");
						continue;
					} else {
						gameBoard[playerRow][playerCol].playerHere = false;
						gameBoard[++playerRow][++playerCol].playerHere = true;	 // Moves player diagonally down, right.
						System.out.println("\nYou moved to the South East.\n");
						endTurn();
						break;
					}
				}
				case "southwest": {
					if (((playerRow + 1) > (gameBoard.length - 1)) || ((playerCol - 1) < 0)) {	  // If the user would move off of the board,
						System.out.println("\nUh oh, it looks like you can't move to the South West.\n");
						continue;
					} else {
						gameBoard[playerRow][playerCol].playerHere = false;
						gameBoard[++playerRow][--playerCol].playerHere = true;	// Moves player diagonally down, left.
						System.out.println("\nYou moved to the South West.\n");
						endTurn();
						break;
					}
				}
				case "northwest": {
					if (((playerRow - 1) < 0) || ((playerCol - 1) < 0)) {	// If the user would move off the board,
						System.out.println("\nUh oh, it looks like you can't move to the North West.\n");
						continue;
					} else {
						gameBoard[playerRow][playerCol].playerHere = false;
						gameBoard[--playerRow][--playerCol].playerHere = true;	// Moves player diagonally up, left.
						System.out.println("\nYou moved to the North West.\n");
						endTurn();
						break;
					}
				}
				case "cancel": {
					System.out.print("\n");
					break;	// Goes back to the menu without ending the turn.
				}
				default: {
					System.out.println("\nPlease choose a direction to move.\n");
					continue;
				}
			}
			break;
		}
	}

	/**
	 * This method lets the player choose an item that they want to locate on the game board
	 * and they are given the items direction from them.  The player cannot search for an item
	 * if they do not have the compass, and they cannot search for an item they have already
	 * found.
	 */
	static void useCompass() {
		if (compassFound) {		// Player has to have found the compass.
			System.out.print("\n");
			int distRow, distCol;
			while (true) {
				// Asks user what they want to search for.
				if (weaponFound) {
					System.out.println(" (1) Weapon - FOUND");
				} else {
					System.out.println(" (1) Weapon");
				}
				if (torchesFound == numTorches) {
					System.out.println(" (2) Torch - FOUND");
				} else if ((torchesFound > 0) && (torchesFound < numTorches)) {
					System.out.println(" (2) Torch " + "FOUND: " + torchesFound);
				} else {
					System.out.println(" (2) Torch ");
				}
				System.out.println(" (3) Wumpus\n (4) Cancel");
				System.out.print("What would you like to search for?: ");
				// Takes user input.
				try {
					userResponse = userInput.next();
				} catch (InputMismatchException e) {
					System.out.println("\nPlease enter one of the given options.\n");
					userInput.next();	// Clears the scanner.
					continue;
				}
				switch (userResponse) {
					case "1": {		// Weapon.
						if (weaponFound) {
							System.out.println("\nYou have already found the weapon.\n");
							continue;
						} else {
							// Finds how far away in each direction the item is from the player.
							distRow = playerRow - weaponRow;
							distCol = playerCol - weaponCol;
							if (distCol > 0) {	  // If item is left of player,
								if (distRow < 0) {	  // If item is below player,
									System.out.println("\nWeapon is to the Southwest.\n");
								} else if (distRow > 0) {	// If item is above player,
									System.out.println("\nWeapon is to the Northwest.\n");
								} else {	// If item is directly to the left,
									System.out.println("\nWeapon is to the West.\n");
								}
							} else if (distCol < 0) {	// If item is right of player,
								if (distRow > 0) {	  // If item is above player,
									System.out.println("\nWeapon is to the Northeast.\n");
								} else if (distRow < 0) {	 // If item is below player,
									System.out.println("\nWeapon is to the Southeast.\n");
								} else {	// If item is directly to the right,
									System.out.println("\nWeapon is to the East.\n");
								}
							} else {
								if (distRow < 0) {	  // If item is directly below,
									System.out.println("\nWeapon is to the South.\n");
								} else {	// If item is directly above,
									System.out.println("\nWeapon is to the North.\n");
								}
							}	
						}
						endTurn();
						break;
					}
					case "2": {		// Torch
						if (torchesFound == numTorches) {
							System.out.println("\nYou have already found all of the torches.\n");
							continue;
						} else {
							// Sorts through the game board to find the closest torch to the player.
							int torchRow = 0, torchCol = 0, distance = Integer.MAX_VALUE;
							for (int row = 0; row < gameBoard.length; row++) {	   // Sorts through each game tile in the board.
								for (int col = 0; col < gameBoard[row].length; col++) {
									if (gameBoard[row][col].torchHere) {
										if (findDistance(row, col) < distance) {	// If this torch is closer to the player than the current closest torch,
											distance = findDistance(row, col);		// Sets the current torch row and col to the closest one.
											torchRow = row;
											torchCol = col;
										}
									}
								}
							}
							// Finds how far away in each direction the item is from the player.
							distRow = playerRow - torchRow;
							distCol = playerCol - torchCol;
							if (distCol > 0) {	  // If item is left of player,
								if (distRow < 0) {	  // If item is below player,
									System.out.println("\nTorch is to the Southwest.\n");
								} else if (distRow > 0) {	// If item is above player,
									System.out.println("\nTorch is to the Northwest.\n");
								} else {	// If item is directly to the left,
									System.out.println("\nTorch is to the West.\n");
								}
							} else if (distCol < 0) {	// If item is right of player,
								if (distRow > 0) {	  // If item is above player,
									System.out.println("\nTorch is to the Northeast.\n");
								} else if (distRow < 0) {	 // If item is below player,
									System.out.println("\nTorch is to the Southeast.\n");
								} else {	// If item is directly to the right,
									System.out.println("\nTorch is to the East.\n");
								}
							} else {
								if (distRow < 0) {	  // If item is directly below,
									System.out.println("\nTorch is to the South.\n");
								} else {	// If item is directly above,
									System.out.println("\nTorch is to the North.\n");
								}
							}	
						}
						endTurn();
						break;
					}
					case "3": {		// Wumpus.
						// Finds how far away in each direction the item is from the player.
						distRow = playerRow - wumpusRow;
						distCol = playerCol - wumpusCol;
						if (distCol > 0) {	  // If item is left of player,
							if (distRow < 0) {	  // If item is below player,
								System.out.println("\nWumpus is to the Southwest.\n");
							} else if (distRow > 0) {	// If item is above player,
								System.out.println("\nWumpus is to the Northwest.\n");
							} else {	// If item is directly to the left,
								System.out.println("\nWumpus is to the West.\n");
							}
						} else if (distCol < 0) {	// If item is right of player,
							if (distRow > 0) {	  // If item is above player,
								System.out.println("\nWumpus is to the Northeast.\n");
							} else if (distRow < 0) {	 // If item is below player,
								System.out.println("\nWumpus is to the Southeast.\n");
							} else {	// If item is directly to the right,
								System.out.println("\nWumpus is to the East.\n");
							}
						} else {
							if (distRow < 0) {	  // If item is directly below,
								System.out.println("\nWumpus is to the South.\n");
							} else {	// If item is directly above,
								System.out.println("\nWumpus is to the North.\n");
							}
						}	
						endTurn();
						break;
					}
					case "4": {		// Cancel.
						// Goes back to menu() without ending the users turn.
						break;
					}
					default: {
						System.out.println("\nPlease enter one of the given options.\n");
						continue;
					}
				}
				break;
			}
		} else {
			System.out.println("\nYou haven't found the compass yet!");
		}
	}

	/**
	 * This method randomly determines whether or not the user wins against the
	 * wumpus by using a percentage that varies off of different variables in the
	 * game.  After the user wins or loses, the game is over and menu() finishes
	 * running.  The user is brought back to the main menu in main().
	 * 
	 * @param oddsOfWinning - The perctentage chance the player has of winning.
	 */
	static void attackWumpus(int oddsOfWinning) {
		if (random.nextInt(100) < oddsOfWinning) { 	// If the user wins,
			System.out.println(" You Beat The Wumpus!\n");
		} else { 	// If the user loses,
			System.out.println(" The Wumpus Ate Your Fingers!\n");
		}
		gameOver = true;	// Causes the game to end and go back to the main menu.
	}
	
	/**
	 * This method returns the distance from the player to another game tile using the distance formula.
	 * The equation used is sqrt((row1 - row2)^2 + (col1 - col2)^2).
	 * 
	 * @return Returns the distance between two game tiles.
	 */
	static int findDistance(int row, int col) {
		return (int)(Math.sqrt(Math.pow((playerRow - row), 2) + Math.pow((playerCol - col), 2)));
	}

}