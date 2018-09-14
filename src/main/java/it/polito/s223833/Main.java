package it.polito.s223833;

import org.apache.log4j.BasicConfigurator;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application
{

	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Layout.fxml"));
			//
			BorderPane rootElement = (BorderPane) loader.load();
			//Creo la scena.
			Scene scene = new Scene(rootElement, 960, 540);
			//Creo lo stage con il titolo dato e con la scena creata in precedenza.
			primaryStage.setTitle("Emotion Detection - Made by Antonio Marceddu");
			primaryStage.setScene(scene);
			setIcon(primaryStage);
			//Mostro la GUI.
			primaryStage.show();
			
			//Log4j setting.
			BasicConfigurator.configure();
			
			//Creo un oggetto di tipo Controller e lo inizializzo.
			Controller controller = loader.getController();
			controller.init();
			
			//Setto un comportamento appropriato per la chiusura dell'applicazione
			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we)
				{
					controller.setClosed();
				}
			}));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		//Carico la libreria nativa di OpenCV.
		//Fortunately, this is unchanged except for one caveat
		nu.pattern.OpenCV.loadShared();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
	
	//Funzione che prova a settare l'icona dell'applicazione.
	private void setIcon(Stage primaryStage)
	{
		try
		{
			Image icon = new Image(getClass().getResource("icon.png").toString());
			primaryStage.getIcons().add(icon);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
