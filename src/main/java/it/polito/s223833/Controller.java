package it.polito.s223833;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import it.polito.s223833.Utils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

/** MODIFIED VERSION of https://github.com/opencv-java/face-detection
 *  by Antonio Marceddu. Original references from the author can be 
 *  found behind.*/

/**
 * The controller associated with the only view of our application. The
 * application logic is implemented here. It handles the button for
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the face detection/tracking.
 * 
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @version 1.1 (2015-11-10)
 * @since 1.0 (2014-01-10)
 * 		
 */

public class Controller 
{
		//Bottoni FXML.
		@FXML
		private Button cameraButton, emotionDetection;
		//Area FXML dove viene mostrato l'ultimo frame.
		@FXML
		private ImageView originalFrame;
		//Radio Buttons FXML.
		@FXML
		private RadioButton CKRadioButton, Fer2013RadioButton;
		//Toggle Groups FXML.
		@FXML
		private ToggleGroup NNGroup;
		//BarChart FXML.
		@FXML
		private BarChart <String, Double> barChart;
		
		// a timer for acquiring the video stream
		private ScheduledExecutorService timer;
		// the OpenCV object that performs the video capture
		private VideoCapture capture;
		// a flag to change the button behavior
		private boolean cameraActive;
		
		//Modello h5.
		MultiLayerNetwork ckModel, fer2013Model; 
		
		// face cascade classifier
		private CascadeClassifier faceCascade;
		private int absoluteFaceSize;
		
		//Altezza e larghezza dell'immagine.
		private int ckDim;
		private int fer2013Dim;

		//Volto salvato.
		private Mat ckFaceToEvaluate;
		private Mat fer2013FaceToEvaluate;
		
		//Serie contenenti le emozioni mostrate su grafico.
		private XYChart.Series<String, Double> emotionSeries;
		
		//Flag che consente di fare la detection solo se la camera è accesa ed è stato rilevato un volto.
		private int flag = 0;
		/**
		 * Init the controller, at start time
		 */
		protected void init()
		{
			this.capture = new VideoCapture();
			this.faceCascade = new CascadeClassifier();
			this.absoluteFaceSize = 0;
			System.out.println("Carico la rete neurale");
			//Creo il Model Loader per il caricamento delle reti neurali preaddestrate.
			ModelLoader modelLoader = new ModelLoader(this);
			//Metto il file per la face detection nella cartella lib esterna: da jar non viene caricato.
			faceCascade = new CascadeClassifier(".\\lib\\haarcascade_frontalface_alt.xml");
			
			//Altezza/larghezza delle immagini di partenza. 
			ckDim=250;
			fer2013Dim=48;

			//Matrice contenente il volto da valutare.
			ckFaceToEvaluate = new Mat();
			fer2013FaceToEvaluate = new Mat();
			
			//Setting del grafico.
			initGraph();
			
			//Avvio il caricamento delle reti neurali preaddestrate.
	    	Thread workerThread = new Thread(modelLoader);
	     	workerThread.start();
		}
		
		/**
		 * The action triggered by pushing the button on the GUI
		 */
		@FXML
		protected void startCamera()
		{	
			//Abilito il pulsante per fare la cattura dell'emozione.
			enableEmotionDetectionButton();	
			if (!this.cameraActive)
			{
				// start the video capture
				this.capture.open(0);
				
				// is the video stream available?
				if (this.capture.isOpened())
				{
					this.cameraActive = true;
					
					// grab a frame every 33 ms (30 frames/sec)
					Runnable frameGrabber = new Runnable() 
					{					
						@Override
						public void run()
						{
							// effectively grab and process a single frame
							Mat frame = grabFrame();
							// convert and show the frame
							Image imageToShow = Utils.mat2Image(frame);
							updateImageView(originalFrame, imageToShow);
						}
					};
					
					this.timer = Executors.newSingleThreadScheduledExecutor();
					this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
					
					// update the button content
					this.cameraButton.setText("Stop Camera");
				}
				else
				{
					// log the error
					System.err.println("Failed to open the camera connection...");
				}
			}
			else
			{
				// the camera is not active at this point
				this.cameraActive = false;
				// update again the button content
				this.cameraButton.setText("Start Camera");
				
				// stop the timer
				this.stopAcquisition();
			}
		}
		
		/**
		 * Get a frame from the opened video stream (if any)
		 * 
		 * @return the {@link Image} to show
		 */
		private Mat grabFrame()
		{
			Mat frame = new Mat();
			
			// check if the capture is open
			if (this.capture.isOpened())
			{
				try
				{
					// read the current frame
					this.capture.read(frame);
					
					// if the frame is not empty, process it
					if (!frame.empty())
					{
						// face detection
						this.detectAndDisplay(frame);
					}
					
				}
				catch (Exception e)
				{
					// log the (full) error
					System.err.println("Exception during the image elaboration: " + e);
				}
			}
			
			return frame;
		}
		
		/**
		 * Method for face detection and tracking
		 * 
		 * @param frame
		 *            it looks for faces in this frame
		 */
		private void detectAndDisplay(Mat frame)
		{
			Rect rectCrop;
			MatOfRect faces = new MatOfRect();
			Mat grayFrame = new Mat();
			
			// convert the frame in gray scale
			Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
			// equalize the frame histogram to improve the result
			Imgproc.equalizeHist(grayFrame, grayFrame);
			
			// compute minimum face size (20% of the frame height, in our case)
			if (this.absoluteFaceSize == 0)
			{
				int height = grayFrame.rows();
				if (Math.round(height * 0.2f) > 0)
				{
					this.absoluteFaceSize = Math.round(height * 0.2f);
				}
			}
			
			//Ricerco volti nell'immagine.
			this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
					new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
					
			//Disegno un rettangolo solo sul primo volto.
			Rect[] facesArray = faces.toArray();
			
			//Se viene rilevato almeno un volto entro.
			if(facesArray.length>0)
			{
				//Salvo solo la prima faccia: se ho una foto con più volti, gli altri vengono persi.
				if(facesArray[0].width>facesArray[0].height)
				{
					rectCrop = new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].width, facesArray[0].width);
				}
				else
				{
					rectCrop = new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].height, facesArray[0].height);
				}
				
				//Creo un'immagine a partire dal ritaglio del volto.
				Mat face = new Mat(grayFrame,rectCrop);
				
				//Creo le versioni dell'immagine a 250x250 pixel per la rete neurale addestrata
				//con CK+ e a 48x48 pixel per la rete neurale addestrata con Fer2013.
				//Scalo la foto in modo che abbiano tutte la stessa dimensione.
				Imgproc.resize(face, ckFaceToEvaluate, new Size(ckDim,ckDim));
				Imgproc.resize(face, fer2013FaceToEvaluate, new Size(fer2013Dim,fer2013Dim));
				//Disegno un rettangolo attorno al volto.
				Imgproc.rectangle(frame, facesArray[0].tl(), facesArray[0].br(), new Scalar(0, 255, 0), 3);
				
				//Abilito il flag: abbiamo delle immagini pronte per la detection!
				flag = 1;	
			}
		}
		
		/**
		 * Stop the acquisition from the camera and release all the resources
		 */
		private void stopAcquisition()
		{
			if (this.timer!=null && !this.timer.isShutdown())
			{
				try
				{
					// stop the timer
					this.timer.shutdown();
					this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					// log any exception
					System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
				}
			}
			
			if (this.capture.isOpened())
			{
				// release the camera
				this.capture.release();
			}
		}
		
		/**
		 * Update the {@link ImageView} in the JavaFX main thread
		 * 
		 * @param view
		 *            the {@link ImageView} to update
		 * @param image
		 *            the {@link Image} to show
		 */
		private void updateImageView(ImageView view, Image image)
		{
			Utils.onFXThread(view.imageProperty(), image);
		}
		
		/**
		 * On application close, stop the acquisition from the camera
		 */
		protected void setClosed()
		{
			this.stopAcquisition();
		}
		
		//Funzione richiamata dal pulsante "Detect Emotion".
		public void detectEmotion()
		{
			//Se ho almeno una lettura di un volto entro.
			if(flag == 1)
			{
				//Blocco la lettura di nuovi frame dalla webcam.
				cameraButton.fire();
				EmotionDetection prediction;
				//Se è selezionato il radio button CK entro qua.
				if(CKRadioButton.isSelected())
				{
					//Converto e mostro l'ultimo frame a video.
					Image imageToShow = Utils.mat2Image(ckFaceToEvaluate);
					updateImageView(originalFrame, imageToShow);
					//Trasformo l'immagine in una BufferedImage.
					BufferedImage bi=Utils.FXImageToAWTBufferedImage(imageToShow);
					//Istanzio un nuovo thread di preparazione dati.
					prediction=new EmotionDetection(this, ckModel, bi, ckDim);
				}
				//Se invece è selezionato il radio button fer2013 entro qua.
				else
				{
					//Converto e mostro l'ultimo frame a video.
					Image imageToShow = Utils.mat2Image(fer2013FaceToEvaluate);
					updateImageView(originalFrame, imageToShow);
					//Trasformo l'immagine in una BufferedImage.
					BufferedImage bi=Utils.FXImageToAWTBufferedImage(imageToShow);
					//Istanzio un nuovo thread di preparazione dati.
					prediction=new EmotionDetection(this, fer2013Model, bi, fer2013Dim);
				}
				//Avvio il worker thread();
		    	Thread workerThread = new Thread(prediction);
		     	workerThread.start();
		     	//Disabilito il pulsante sino al termine dell'analisi.
		     	emotionDetection.setDisable(true);
			}
	     }
		
		//Funzione di inizializzazione del grafico.
		private void initGraph()
		{						
			//Inizializzo la serie mostrata su grafico e setto il suo nome.
	        emotionSeries = new XYChart.Series<String, Double>();
	        emotionSeries.setName("Emotions");

	        emotionSeries.getData().add(new XYChart.Data<String, Double>("Anger", 0.0));
	        emotionSeries.getData().add(new XYChart.Data<String, Double>("Disgust", 0.0));
	        emotionSeries.getData().add(new XYChart.Data<String, Double>("Fear", 0.0));
	        emotionSeries.getData().add(new XYChart.Data<String, Double>("Happiness", 0.0));
	        emotionSeries.getData().add(new XYChart.Data<String, Double>("Neutral", 0.0));
	        emotionSeries.getData().add(new XYChart.Data<String, Double>("Sadness", 0.0));
	        emotionSeries.getData().add(new XYChart.Data<String, Double>("Surprise", 0.0));        
	        
	        //hack-around per bug con l'auto-range dei CategoryAxis.
	        ((CategoryAxis) barChart.getXAxis()).setCategories(FXCollections.observableArrayList("Anger", "Disgust", "Fear", "Happiness", "Neutral", "Sadness", "Surprise"));
	        
	        //Aggiungo la serie al grafico.
	        barChart.getData().add(emotionSeries);
		}		
		
		//Funzione di setting dei valori del grafico.
		public void setGraphValues(double anger, double disgust, double fear, double happiness, double neutral, double sadness, double surprise)
		{
			XYChart.Data<String, Double> angryXYchart = emotionSeries.getData().get(0);
			angryXYchart.setYValue(Math.floor(anger * 100) / 100);
			XYChart.Data<String, Double> disgustXYchart = emotionSeries.getData().get(1);
			disgustXYchart.setYValue(Math.floor(disgust * 100) / 100);
			XYChart.Data<String, Double> fearXYchart = emotionSeries.getData().get(2);
			fearXYchart.setYValue(Math.floor(fear * 100) / 100);
			XYChart.Data<String, Double> happinessXYchart = emotionSeries.getData().get(3);
			happinessXYchart.setYValue(Math.floor(happiness * 100) / 100);
			XYChart.Data<String, Double> neutralXYchart = emotionSeries.getData().get(4);
			neutralXYchart.setYValue(Math.floor(neutral * 100) / 100);
			XYChart.Data<String, Double> sadnessXYchart = emotionSeries.getData().get(5);
			sadnessXYchart.setYValue(Math.floor(sadness * 100) / 100);
			XYChart.Data<String, Double> surpriseXYchart = emotionSeries.getData().get(6);
			surpriseXYchart.setYValue(Math.floor(surprise * 100) / 100);
		}
		
		//Funzione di attivazione di tutti i pulsanti.
		public void enableButtons()
		{
			cameraButton.setDisable(false);
			emotionDetection.setDisable(false);
		}	
		
		//Funzione di riattivazione del pulsante "Detect Emotion".
		public void enableEmotionDetectionButton()
		{
	     	emotionDetection.setDisable(false);
		}	
		
		//Funzione di setting dei modelli caricati tramite la classe ModelLoader.
		public void setModels(MultiLayerNetwork model1, MultiLayerNetwork model2)
		{
			ckModel=model1;
			fer2013Model=model2;
		}	
	}