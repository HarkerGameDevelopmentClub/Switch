package main;

import javafx.animation.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.image.*;
import javafx.stage.*;
import javafx.event.*;
import java.net.URL;
import java.util.*;

/**
 * The main class handles everything rn
 * @author Praveen
 *
 */
public class Main extends Application implements EventHandler<KeyEvent>
{
	private Canvas canvas;
	private GraphicsContext context;

	private static double WALL_WIDTH = 50;
	private static double WINDOW_WIDTH = 800;
	private static double WINDOW_HEIGHT = 600;

	private static double PLAYER_SIZE = 50;
	private static double PLAYER_SPEED_X = 10;
	private static double PLAYER_SPEED_UP = -2; // negative means up
	private static double PLAYER_SPEED_DOWN = 1;

	private boolean playerGoingLeft = false;
	private boolean playerAtWall = true;
	private double playerX = WINDOW_WIDTH - WALL_WIDTH;
	private double playerY = WINDOW_HEIGHT / 2;

	private FrameTimer timer;
	private int currentFrame = 0;

	private MediaPlayer player;

	private static double COIN_SPEED = 3;
	private static double COIN_SIZE = 30;

	private ArrayList<Double> coins = new ArrayList<Double>();
	private int score = 0;

	private static double SPIKE_SIZE = 50;
	private static double SPIKE_SPEED = 2;

	private ArrayList<Double> spikes = new ArrayList<Double>();

	private int dead = 0;
	private boolean clearScreen = true;

	/**
	 * Get the game started
	 * @param args useless
	 */
	public static void main(String[] args)
	{
		launch(args);
	}

	/**
	 * Start the game
	 * @param stage the stage
	 */
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("switch");
		stage.setResizable(false);

		canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
		context = canvas.getGraphicsContext2D();

		Scene s = new Scene(new Group(canvas));
		s.setOnKeyPressed(this);
		//		s.setOnKeyReleased(this);

		stage.setScene(s);
		stage.show();

		timer = new FrameTimer();
		timer.start();

		// Flappy bird's play music code, adapted for Switch
		URL path = getClass().getResource("/bgm.mp3");
		Media music = new Media(path.toString());
		player = new MediaPlayer(music);
		player.setCycleCount(MediaPlayer.INDEFINITE);
		player.setAutoPlay(true);
	}

	/**
	 * Used to call the nextFrame every so often
	 * @author Praveen
	 *
	 */
	public class FrameTimer extends AnimationTimer {
		public void handle(long now)
		{
			nextFrame();
		}
	}

	/**
	 * Call this one to compute and render the next frame
	 */
	private void nextFrame()
	{
		currentFrame = (currentFrame + 1) % (Integer.MAX_VALUE - 1); // the -1 might not be needed
		updateState();
		drawFrame();
	}

	/**
	 * Update the state data for the next frame
	 */
	private void updateState()
	{
		//DONT DO ANYTHNG if dead
		if (dead > 0)
		{
			dead--;
			return;
		}
		// update player state
		if (playerGoingLeft) // q = go left
			playerAtWall = playerX == WALL_WIDTH;
		else
			playerAtWall = playerX == WINDOW_WIDTH - WALL_WIDTH - PLAYER_SIZE;

		// move player
		playerY += playerAtWall ? PLAYER_SPEED_UP : PLAYER_SPEED_DOWN;
		playerX += PLAYER_SPEED_X * (playerGoingLeft ? -1 : 1);

		//Crop player position
		playerX = mid(WALL_WIDTH, playerX, WINDOW_WIDTH-WALL_WIDTH-PLAYER_SIZE);
		playerY = mid(0, playerY, WINDOW_HEIGHT - PLAYER_SIZE);

		//Update coins
		for (int i = coins.size() - 1; i >= 1; i -= 2)
		{
			if (squaresAreColliding(coins.get(i-1), coins.get(i), COIN_SIZE, playerX + 10, playerY + 10, PLAYER_SIZE - 20))
			{
				coins.remove(i);
				coins.remove(i-1);
				score++;
			}
			else
			{
				Double newY = new Double(coins.get(i).doubleValue() + COIN_SPEED);
				if (newY < WINDOW_HEIGHT - SPIKE_SIZE)
					coins.set(i, newY);
				else
				{
					coins.remove(i);
					coins.remove(i-1);
				}
			}
		}
		//Add new coins
		if (currentFrame % 60 == 0)
		{
			if (Math.random() < 0.5)
			{
				boolean upward = Math.random() >= 0.5;
				int imax = (int) ((WINDOW_WIDTH - 2 * WALL_WIDTH) / COIN_SIZE);
				for (int i = 0; i < imax; i++)
				{
					if (Math.random() > 0.2)
					{
						coins.add(new Double(WALL_WIDTH + i * COIN_SIZE));
						if (upward)
							coins.add(new Double(-i * COIN_SIZE/4));
						else
							coins.add(new Double(-(imax - i) * COIN_SIZE/4));
					}
				}
			}
		}
		//Add new spikes
		if (currentFrame % 60 == 0 && Math.random() < 0.5)
		{
			boolean left = Math.random() >= 0.5;
			spikes.add(left ? WALL_WIDTH : WINDOW_WIDTH - WALL_WIDTH - SPIKE_SIZE);
			spikes.add(-SPIKE_SIZE);
		}
		//Move existing spikes
		for (int i = spikes.size() - 1; i >= 1; i -= 2)
		{
			double spikeY = spikes.get(i);
			if (spikeY > WINDOW_HEIGHT - SPIKE_SIZE)
			{
				spikes.remove(i);
				spikes.remove(i-1);
			}
			else
			{
				spikes.set(i, spikeY + SPIKE_SPEED);
			}
		}
		//Collision with spike check
		for (int i = 0; i < spikes.size() -1; i += 2)
		{
			if (squaresAreColliding(spikes.get(i), spikes.get(i+1), SPIKE_SIZE, playerX + 10, playerY + 10, PLAYER_SIZE - 20))
			{
				die();
			}
		}
		//Collision with bottom row spikes
		if (playerY + PLAYER_SIZE - 10 > WINDOW_HEIGHT - SPIKE_SIZE)
			die();
	}

	/**
	 * Rendering call.
	 * @precondition the state data has been updated
	 */
	private void drawFrame()
	{
		// Clear the previous frame
		if (clearScreen)
		{
			tileDraw(new Image("/bg.png"), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		}
		// Death screen
		if (dead > 0)
		{
			context.setFill(Color.RED);
			context.fillText("Game over at " + score + " points. Try again in " + dead, 40, WINDOW_HEIGHT/2);
			return;
		}
		// Draw the walls
		Image wallImage = new Image("/wall.png");
		tileDraw(wallImage, 0, 0, WALL_WIDTH, WINDOW_HEIGHT);
		tileDraw(wallImage, WINDOW_WIDTH-WALL_WIDTH, 0, WALL_WIDTH, WINDOW_HEIGHT);
		// Draw the wagon
		String wagonName = playerAtWall ? "wagon" : "thrust";
		wagonName += playerGoingLeft ? "l" : "r";
		context.drawImage(new Image("/" + wagonName + ".png"), playerX, playerY);
		// Draw the coins
		int coinsSize = coins.size();
		for (int i = 0; i < coinsSize - 1; i += 2)
			context.drawImage(new Image("/coin.png"), coins.get(i).doubleValue(), coins.get(i+1).doubleValue(), COIN_SIZE, COIN_SIZE);
		// Draw the spikes
		int spikesSize = spikes.size();
		for (int i = 0; i < spikesSize - 1; i += 2)
			context.drawImage(new Image("/spike.png"), spikes.get(i).doubleValue(), spikes.get(i+1).doubleValue(), SPIKE_SIZE, SPIKE_SIZE);
		// Draw immobile spikes
		tileDraw(new Image("/spike.png"), 0, WINDOW_HEIGHT - SPIKE_SIZE, WINDOW_WIDTH, SPIKE_SIZE);
		// Draw score
		context.setFill(Color.WHITE);
		context.fillText("score: " + score, 5, 20);
	}

	/**
	 * Keyboard listener sets the key toggles
	 * @param event the keyboard events
	 */
	@Override
	public void handle(KeyEvent event) {
		if (event.getText().toLowerCase().equals("q"))
			clearScreen = !clearScreen;
		else
			playerGoingLeft = !playerGoingLeft;
	}

	/**
	 * Draws a tiled image that will fill
	 * the bounds completely even if this means
	 * that it gets cut off at the edge
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void tileDraw(Image image, double x, double y, double width, double height)
	{
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();

		double numDrawsY = (int) height / (int) imageHeight;
		double numDrawsX = (int) width / (int) imageWidth;

		double heightLeftOver = (int) height % (int) imageHeight;
		double widthLeftOver = (int) width % (int) imageWidth;

		// All of the complete tiles
		for (int i = 0; i < numDrawsY; i++)
			for (int j = 0; j < numDrawsX; j++)
				context.drawImage(image,x + j*imageWidth, y + i*imageHeight,imageWidth, imageHeight);

		// Vertically cropped tiles
		for (int i = 0; i < numDrawsX; i++)
			context.drawImage(image, 0, 0, imageWidth, heightLeftOver,
					x + i*imageWidth, y + numDrawsY*imageHeight, imageWidth, heightLeftOver);
		// Horizontally cropped tiles
		for (int i = 0; i < numDrawsY; i++)
			context.drawImage(image, 0, 0, widthLeftOver, imageHeight,
					x + numDrawsX*imageWidth, y + i*imageHeight, widthLeftOver, imageHeight);
		// Doubly cropped tile
		context.drawImage(image, 0, 0, widthLeftOver, heightLeftOver,
				x + numDrawsX*imageWidth, y + numDrawsY*imageHeight, widthLeftOver, heightLeftOver);
	}

	/**
	 * Get the middle value of the three (a, b, c) useful for cropping a value within bounds.
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private double mid(double a, double b, double c)
	{
		double max = Math.max(Math.max(a,  b), c);
		double min = Math.min(Math.min(a, b), c);
		return a + b + c - max - min; // the one left over is the min
	}

	/**
	 * Don't know if this works as it should
	 */
	private boolean areColliding(double x1min, double y1min, double x1max, double y1max, double x2min, double y2min, double x2max, double y2max)
	{
		boolean xCollision = (mid(x1min, x2min, x2max) == x1min)
				|| (mid(x1max, x2min, x2max) == x1max)
				|| (mid(x2min, x1min, x1max) == x2min)
				|| (mid(x2max, x1min, x1max) == x2max)
				;
		boolean yCollision = (mid(y1min, y2min, y2max) == y1min)
				|| (mid(y1max, y2min, y2max) == y1max)
				|| (mid(y2min, y1min, y1max) == y2min)
				|| (mid(y2max, y1min, y1max) == y2max)
				;
		return xCollision && yCollision;
	}

	private boolean squaresAreColliding(double x1min, double y1min, double size1, double x2min, double y2min, double size2)
	{
		return areColliding(x1min, y1min, x1min+size1, y1min+size2, x2min, y2min, x2min+size2, y2min+size2);
	}

	private void die()
	{
		dead = 180;
		playerGoingLeft = false;
		playerAtWall = true;
		playerX = WINDOW_WIDTH;
		playerY = WINDOW_HEIGHT/2;
		coins.clear();
		spikes.clear();
	}

}