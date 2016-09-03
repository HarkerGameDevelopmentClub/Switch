package main;

import javafx.animation.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
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
	private static double WINDOW_WIDTH = 400;
	private static double WINDOW_HEIGHT = 600;

	private static double PLAYER_SIZE = 50;
	private static double PLAYER_SPEED_X = 10;
	private static double PLAYER_SPEED_UP = -1; // negative means up
	private static double PLAYER_SPEED_DOWN = 2;
	
	private enum PlayerState {LEFT_WALL, RIGHT_WALL, LEFT_AIR, RIGHT_AIR};
	private PlayerState playerState;
	private double playerX = WINDOW_WIDTH - WALL_WIDTH;
	private double playerY = WINDOW_HEIGHT / 2;

	private FrameTimer timer;
	private int currentFrame = 0;
	private TreeSet<String> keysPressed = new TreeSet<String>();

	private MediaPlayer player;

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
		//		stage.setResizable(false);

		canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
		context = canvas.getGraphicsContext2D();

		Scene s = new Scene(new Group(canvas));
		s.setOnKeyPressed(this);
		s.setOnKeyReleased(this);

		stage.setScene(s);
		stage.show();

		timer = new FrameTimer();
		timer.start();

		// Play music routine from Flappy Bird
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
		// update player state
		if (isKeyPressed("q")) // q = go left
		{
			boolean atLeftWall = (playerX == WALL_WIDTH);
			System.out.println(atLeftWall);
			playerState = atLeftWall ? PlayerState.LEFT_WALL : PlayerState.LEFT_AIR;
		}
		else
		{
			boolean atRightWall = (playerX == WINDOW_WIDTH - WALL_WIDTH - PLAYER_SIZE);
			playerState = atRightWall ? PlayerState.RIGHT_WALL : PlayerState.RIGHT_AIR;
		}
		
		// move player
		playerY += playerAtWall() ? PLAYER_SPEED_UP : PLAYER_SPEED_DOWN;
		playerX += PLAYER_SPEED_X * (playerGoingLeft() ? -1 : 1);
		
		//Crop player position
		playerX = mid(WALL_WIDTH, playerX, WINDOW_WIDTH-WALL_WIDTH-PLAYER_SIZE);
		playerY = mid(0, playerY, WINDOW_HEIGHT - PLAYER_SIZE);
	}

	/**
	 * Rendering call.
	 * @precondition the state data has been updated
	 */
	private void drawFrame()
	{
		// Clear the previous frame
		if (currentFrame % 3 == 0)
		{
			context.setFill(Color.GRAY);
			context.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		}
		// Draw the walls
		Image wallImage = new Image("/wall.png");
		tileDraw(wallImage, 0, 0, WALL_WIDTH, WINDOW_HEIGHT);
		tileDraw(wallImage, WINDOW_WIDTH-WALL_WIDTH, 0, WALL_WIDTH, WINDOW_HEIGHT);
		
		String wagonName = null;
		switch (playerState)
		{
		case LEFT_AIR:
			wagonName = "thrustl";
			break;
		case LEFT_WALL:
			wagonName = "wagonl";
			break;
		case RIGHT_AIR:
			wagonName = "thrustr";
			break;
		case RIGHT_WALL:
			wagonName = "wagonr";
			break;
		}
		context.drawImage(new Image("/" + wagonName + ".png"), playerX, playerY);
	}

	/**
	 * Keyboard listener sets the key toggles
	 * @param event the keyboard events
	 */
	@Override
	public void handle(KeyEvent event) {
		String key = event.getText().toLowerCase(); // shift key should not affect
		EventType<KeyEvent> type = event.getEventType();
		if (type == KeyEvent.KEY_PRESSED)
			keysPressed.add(key);
		else if (type == KeyEvent.KEY_RELEASED)
			keysPressed.remove(key);
	}

	public boolean isKeyPressed(String key)
	{
		return keysPressed.contains(key);
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
	public double mid(double a, double b, double c)
	{
		double max = Math.max(Math.max(a,  b), c);
		double min = Math.min(Math.min(a, b), c);
		return a + b + c - max - min; // the one left over is the min
	}
	
	private boolean playerAtWall()
	{
		return playerState == PlayerState.LEFT_WALL || playerState == PlayerState.RIGHT_WALL;
	}
	
	private boolean playerGoingLeft()
	{
		return playerState == PlayerState.LEFT_WALL || playerState == PlayerState.LEFT_AIR;
	}

}