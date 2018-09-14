package it.polito.s223833;

import java.awt.image.BufferedImage;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class EmotionDetection implements Runnable
{ 
	Controller controller;
	MultiLayerNetwork model;
	BufferedImage image;
	int size;
	
	@Override
	public void run() 
	{
		try
		{
			System.out.println("Converto l'immagine");
            //Uso il nativeImageLoader per convertire l'immagine in una matrice numerica.
            NativeImageLoader loader = new NativeImageLoader(size, size, 1);
			System.out.println("Creo l'input");
            //Metto l'immagine in un INDArray.
            INDArray input = loader.asMatrix(image);
			System.out.println("Predizione");
	    	//Faccio valutare dal modello l'immagine.
			INDArray output = model.output(input);
	
		 	System.out.println("Predizione completata.");
			System.out.println(output.toString());
				
			//Aggiorno il grafico e riabilito il pulsante "Detect Emotion".
			Platform.runLater(() -> {
				controller.setGraphValues(output.getDouble(0),output.getDouble(1),output.getDouble(2),output.getDouble(3),output.getDouble(4),output.getDouble(5),output.getDouble(6));
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			//Riabilito il pulsante "Detect Emotion".
			Platform.runLater(() -> 
			{
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Attention!");
				alert.setHeaderText("Emotion detection error");
				alert.setContentText("There was an error during the emotion detection process.");
				//Chiudo il programma al click.
				Platform.exit();
			});
		}		
	}	

	public EmotionDetection(Controller controller, MultiLayerNetwork model, BufferedImage image, int size)
	{
		this.controller=controller;
		this.model=model;
		this.image=image;
		this.size=size;
	}
}
