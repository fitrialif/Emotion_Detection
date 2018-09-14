package it.polito.s223833;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ModelLoader implements Runnable
{ 
	final ProgressBar pb;
	Stage stage;
	Controller controller;
	
	@Override
	public void run() 
	{		
		try 
		{
			//Carico la rete neurale preaddestrata con CK+.
			MultiLayerNetwork ckModel = KerasModelImport.importKerasSequentialModelAndWeights(".\\lib\\ck+_emotion_detection_model.h5");
			Platform.runLater(() -> {
				//Aggiorno il progresso.
				pb.setProgress(50.0);
			});
			System.out.println("Rete neurale preaddestrata con CK+ caricata.");
			//Carico la rete neurale preaddestrata con Fer2013+.
			MultiLayerNetwork fer2013Model = KerasModelImport.importKerasSequentialModelAndWeights(".\\lib\\fer2013_emotion_detection_model.h5");
			System.out.println("Rete neurale preaddestrata con Fer2013 caricata.");
			Platform.runLater(() -> {
				//Aggiorno il progresso.
				pb.setProgress(100.0);
				//Setto i modelli.
				controller.setModels(ckModel,fer2013Model);
				//Abilito i pulsanti.
				controller.enableButtons();
				//Chiudo lo stage.
				stage.close();
			});
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			Platform.runLater(() -> {
				//Chiudo lo stage.
				stage.close();
				//Preparo una finestra di alert.
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Attention!");
				alert.setHeaderText("Model Loading Error");
				alert.setContentText("There was an error during the Neural Network Model loading. Please make sure that the ck+_emotion_detection_model.h5 and the fer2013_emotion_detection_model.h5 files are in the lib folder and restart the program.");

				alert.showAndWait();
				//Chiudo il programma al click.
				Platform.exit();
			});
		}
	}	

	public ModelLoader(Controller controller)
	{
		this.controller=controller;	
		//Creo uno stage.
		stage = new Stage();
		//Rimuovo i pulsanti della barra superiore.
		stage.initStyle(StageStyle.UNDECORATED);
		
		Group root = new Group();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		
		//Creo una label.
		Label label = new Label();
		label.setText("Loading Neural Network Models...");
		//Creo una progressbar.		         
		pb = new ProgressBar(0);

		//Creo una vbox e inserisco al suo interno la label e la progressbar.
		final VBox vb = new VBox();
		vb.setPrefSize(250, 50);
		vb.setAlignment(Pos.CENTER);
		vb.getChildren().add(label);
		vb.getChildren().add(pb);
		scene.setRoot(vb);
		//Mostro lo stage.
		stage.show();   
	}
}