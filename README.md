# Emotion Detection

<p align="center">
	Screenshot of the Emotion Detection program.
   <img src="https://github.com/AnthonyMarc/Emotion_Detection/blob/master/resources/EmotionDetection.png"> 
</p>

Description available in english and italian below.

## English
Emotion Detection is a project realized by me for the Computer Vision exam of the Politecnico di Torino, together with the Dataset Preparation programs, available through the links at the bottom; it was built in Java using the Eclipse development environment, and uses Apache Maven, the OpenCV and DL4J libraries and the Maven JavaFX plugin.

This program allows you to capture the user's face via webcam, make a prediction of the emotion among the 7 available (anger, disgust, fear, happiness, neutrality, sadness and surprise) and show the results; for design choices, it is markedly single-user: if there are more people in the captured image, only one face will be selected and processed.

The software has a clean interface, with:
- a large box to show images captured by webcam;
- a histogram, showing the results of the prediction carried out by the neural network;
- two radio-type buttons, which make it possible to make a mutually exclusive choice on the type of pre-trained neural network to be used (CK + or Fer2013);
- two classic buttons: "Start Camera", which allows you to activate and deactivate the webcam, and "Detect Emotion", which initiates the prediction of the emotional state of the person whose face has been captured through the algorithm of Haar.

<p align = "center">
Emotion Detection: happiness case.
   <img src="https://github.com/AnthonyMarc/Emotion_Detection/blob/master/resources/Happiness.png">
</ P>

The program is testable by creating a Maven configuration with goal jfx:run: a .jar file will be created and executed in the /target/jfx/app/ directory; for working correctly, it is necessary that in the ./lib/ folder relative to the file .jar position there are:
- the .xml file containing the information for the Haar detection (haarcascade_frontalface_alt.xml);
- the file containing the pre-trained neural network with the CK + dataset (ck + _emotion_detection_model.xml);
- the file containing the pre-trained neural network with the Fer2013 dataset (Fer2013_emotion_detection_model.xml).

These files are located in the /lib/ folder of the repository: before creating the config, copy the whole folder to the /target/jfx/app/ location.

We can see that networks tend to give clear results: this is also a symptom of a not perfect training, probably due to a combination of factors, including datasets. It would have been useful to test neural networks with datasets often used as tests, such as CIFAR-10, but I preferred to use the time available to experiment the behavior of datasets with different structures of neural networks.

<p align="center">
	Emotion Detection: anger case.
   <img src="https://github.com/AnthonyMarc/Emotion_Detection/blob/master/resources/Anger.png"> 
</p>

## Italiano
Emotion Detection è un progetto realizzato da me per l'esame di Computer Vision del Politecnico di Torino, assieme ai programmi di Dataset Preparation, reperibili tramite i link in fondo; esso è stato realizzato in Java usando come ambiente di sviluppo Eclipse, e fa uso di Apache Maven, delle librerie OpenCV e DL4J e del plugin JavaFX Maven. 

Questo programma consente di catturare il volto dell'utente tramite webcam, fare una predizione dell'emozione tra le 7 disponibili (rabbia, disgusto, paura, felicità, neutralità, tristezza e sorpresa) e mostrarne i risultati; esso, per scelte progettuali, è marcatamente monoutente: se nell'immagine catturata son presenti più persone, verrà selezionato e processato solamente un volto.

Il software presenta un'interfaccia pulita, con:
- un ampio riquadro per mostrare le immagini catturate tramite webcam;
- un istogramma, riportante i risultati della predizione effettuata dalla rete neurale;
- due bottoni di tipo radio, che consentono di fare una scelta mutualmente esclusiva sul tipo di rete neurale preaddestrata da utilizzare (CK+ o Fer2013);
- due bottoni classici: "Start Camera", che consente di attivare e disattivare la webcam, ed "Detect Emotion", che avvia la predizione dello stato emozionale della persona il cui volto è stato catturato tramite l'algoritmo di Haar.

<p align="center">
	Emotion Detection: sadness case.
   <img src="https://github.com/AnthonyMarc/Emotion_Detection/blob/master/resources/Sadness.png"> 
</p>

Il programma è testabile creando una configurazione Maven con goal jfx:run: verrà creato ed eseguito un file .jar nella cartella /target/jfx/app/; per il corretto funzionamento, è necessario che nella cartella ./lib/ relativa alla posizione file .jar siano presenti:
- il file .xml contenenti le informazioni per la Haar detection (haarcascade_frontalface_alt.xml)
- il file contenente la rete neurale preaddestrata con il dataset CK+ (ck+_emotion_detection_model.xml)
- il file contenente la rete neurale preaddestrata con il dataset Fer2013 (Fer2013_emotion_detection_model.xml)

Questi file son presenti nel repository e si trovano nella cartella /lib/: prima di creare la configuazione, copiare tutta la cartella nella posizione /target/jfx/app/.

Possiamo notare che le reti tendono a dare risultati netti: anche questo è un sintomo di un addestramento non perfetto, probabilmente dovuto ad un'insieme di fattori, dataset compresi. Sarebbe stato utile testare le reti neurale con dataset spesso utilizzati come test, come il CIFAR-10, ma ho preferito usare il tempo a mia disposizione per sperimentare il comportamento dei dataset con diverse strutture di reti neurali.

### Links:
Dataset Preparation for Fer2013: https://github.com/AnthonyMarc/Dataset_Preparation_For_Fer2013

Dataset Preparation for CK+: https://github.com/AnthonyMarc/Dataset_Preparation_For_CK
