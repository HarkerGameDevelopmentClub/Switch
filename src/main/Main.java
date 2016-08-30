package main;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.stage.*;
import javafx.event.*;

public class Main extends Application implements EventHandler<KeyEvent>
{
	Canvas canvas;
	GraphicsContext context;
	double PlayerX;
	double PlayerY;
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("switch");
		stage.setResizable(false);

		canvas = new Canvas(400, 600);
		context = canvas.getGraphicsContext2D();

		Scene s = new Scene(new Group(canvas));
		s.setOnKeyPressed(this);
		
		stage.setScene(s);
		stage.show();
	}
	
	public void drawFrame()
	{
		context.setFill(Color.WHITE);
		context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		Color col = Color.color(Math.random(), Math.random(), Math.random());
		context.setFill(col);
		context.fillRect(Math.random()*400 - 80, Math.random()*600 - 80, Math.random() * 200, Math.random() * 300);
	}

	@Override
	public void handle(KeyEvent event) {
		String key = event.getText();
		if (key.toLowerCase().equals("d"))
			drawFrame();
	}
}