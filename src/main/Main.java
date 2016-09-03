package main;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.stage.*;
import javafx.event.*;

/**
 * The main class handles everything rn
 * @author Praveen
 *
 */
public class Main extends Application implements EventHandler<KeyEvent>
{
	Canvas canvas;
	GraphicsContext context;
	
	static double WALL_WIDTH = 70;
	static double WINDOW_WIDTH = 400;
	static double WINDOW_HEIGHT = 570;
	
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
		
		stage.setScene(s);
		stage.show();
	}
	
	/**
	 * Call this one to compute and render the next frame
	 */
	public void nextFrame()
	{
		computeFrame();
		drawFrame();
	}
	
	/**
	 * Update the state data for the next frame
	 */
	public void computeFrame()
	{
	}
	
	/**
	 * Rendering call.
	 * @precondition the state data has been updated
	 */
	public void drawFrame()
	{
		// Clear the previous frame
		context.setFill(Color.GRAY);
		
		// Draw the walls
		Image wallImage = new Image("/wall.png");
		tileDraw(wallImage, 0, 0, WALL_WIDTH, WINDOW_HEIGHT);
		tileDraw(wallImage, WINDOW_WIDTH-WALL_WIDTH, 0, WALL_WIDTH, WINDOW_HEIGHT);
	}

	/**
	 * Keyboard listener sets the key toggles
	 * @param event the keyboard events
	 */
	@Override
	public void handle(KeyEvent event) {
		String key = event.getText();
		if (key.toLowerCase().equals("d"))
			drawFrame();
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
	public void tileDraw(Image image, double x, double y, double width, double height)
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
	
}