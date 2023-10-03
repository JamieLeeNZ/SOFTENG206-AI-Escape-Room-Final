package nz.ac.auckland.se206.controllers.puzzles;

import java.lang.reflect.Field;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.HintManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.constants.Algorithm;
import nz.ac.auckland.se206.constants.Description;
import nz.ac.auckland.se206.constants.GameState;
import nz.ac.auckland.se206.constants.GameState.Difficulty;
import nz.ac.auckland.se206.constants.Sequence;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.utilities.Timer;

/** Controller class for the decryption puzzle scene. */
public class DecryptionPuzzleController {
  @FXML private Pane paBack;
  @FXML private Pane paHint;
  @FXML private Pane paEmpty;
  @FXML private Pane paAnalyze;
  @FXML private Pane paPassword;
  @FXML private Pane paDecryption;
  @FXML private Pane paBackOverlay;

  @FXML private Label lblTime;
  @FXML private Label lblEmpty;
  @FXML private Label lblMemory;
  @FXML private Label lblPassword;
  @FXML private Label lblHintCounter;

  @FXML private Polygon pgEmptyLeft;
  @FXML private Polygon pgEmptyRight;
  @FXML private Polygon pgPasswordLeft;
  @FXML private Polygon pgPasswordRight;

  @FXML private TextArea taChat;
  @FXML private TextArea taAlgorithm;
  @FXML private TextArea taDescription;

  private int hintIndex;
  private int psuedocodeIndex;

  private double memoryUsed;

  private boolean isPasswordTabOpen;

  private String sequence;
  private String algorithm;
  private String pseudocode;
  private String description;

  private ChatCompletionRequest gptRequest;

  private TextToSpeech tts;

  /** Initializes the decryption puzzle. */
  @FXML
  private void initialize() throws Exception {
    // Password tab is initially open
    isPasswordTabOpen = true;

    // get the game state instance of tts
    this.tts = GameState.tts;

    // Add the label to list of labels to be updated
    Timer.addLabel(lblTime);

    // Initialize the memory component
    initializeMemory();

    // Initialize the pseudocode and algorithms
    initializePseudocode();
  }

  /** When the mouse is hovering over the pane, the overlay appears (hint). */
  @FXML
  private void onHintPaneEntered() {
    paHint.setStyle("-fx-background-color: rgb(29,30,37);");
  }

  /** When the mouse is not hovering over the pane, the overlay disappears (hint). */
  @FXML
  private void onHintPaneExited() {
    paHint.setStyle("-fx-background-color: rgb(20,20,23);");
  }

  /** When the mouse is hovering over the pane, the overlay appears (analyze). */
  @FXML
  private void onAnalyzeEntered() {
    paAnalyze.setStyle("-fx-background-color: rgb(29,30,37);");
  }

  /** When the mouse is not hovering over the pane, the overlay disappears (analyze). */
  @FXML
  private void onAnalyzeExited() {
    paAnalyze.setStyle("-fx-background-color: rgb(20,20,23);");
  }

  /** When the mouse is hovering over the pane, the overlay appears (back). */
  @FXML
  private void onBackPaneEntered() {
    paBack.setStyle("-fx-background-color: rgb(29,30,37);");
  }

  /** When the mouse is not hovering over the pane, the overlay disappears (back). */
  @FXML
  private void onBackPaneExited() {
    paBack.setStyle("-fx-background-color: rgb(20,20,23);");
  }

  /** When the mouse is hovering over the pane, the overlay appears (empty). */
  @FXML
  private void onEmptyEntered() {
    enableEmptyTabComponents();
  }

  /** When the mouse is not hovering over the pane, the overlay disappears (empty). */
  @FXML
  private void onEmptyExited() {
    // Check if the empty tab is already opened
    if (!isPasswordTabOpen) {
      return;
    }

    // disable empty tab components
    disableEmptyTabComponents();
  }

  /** When the mouse is hovering over the pane, the overlay appears (password). */
  @FXML
  private void onPasswordEntered() {
    enablePasswordTabComponents();
  }

  /** When the mouse is not hovering over the pane, the overlay disappears (password). */
  @FXML
  private void onPasswordExited() {
    // Check if the password tab is already open
    if (isPasswordTabOpen) {
      return;
    }

    // disable password tab components
    disablePasswordTabComponents();
  }

  /** When hint is clicked, give the user a hint. */
  @FXML
  private void onHintPaneClicked() {
    // If the difficulty is hard, ignore user.
    if (GameState.gameDifficulty == Difficulty.HARD) {
      return;
    }

    // If the number of remaining hints is zero
    if (GameState.hintCounter == 0) {
      return;
    }

    getUserHint();
  }

  /** When back is clicked, go back to previous section (control room). */
  @FXML
  private void onBackPaneClicked() {
    App.setUi(AppUi.TERMINAL);
  }

  private void initializeMemory() {
    // Initialize memory used to zero
    memoryUsed = 0;

    // Create a grid of memory cells
    for (double y = 0; y < 5; y++) {
      for (double x = 0; x < 15; x++) {
        setMemoryLocation(x, y, 5, 5);
      }
    }

    // Recalculate memory used using ratio
    memoryUsed = getMemoryUsed();

    // Set the memory used text
    lblMemory.setText("USING " + memoryUsed + " OUT OF 32 GiB");
  }

  @FXML
  private void onEmptyClicked() {
    // Empty tab is now open
    isPasswordTabOpen = false;

    // Set the empty label to 'opened tab' color
    lblEmpty.setTextFill(Color.rgb(80, 170, 255));

    // Set the password label to 'closed tab' color
    lblPassword.setTextFill(Color.rgb(255, 255, 255));

    // Disable password tab components
    disablePasswordTabComponents();
  }

  @FXML
  private void onPasswordClicked() {
    // Password tab is now open
    isPasswordTabOpen = true;

    // Set the empty label to 'closed tab' color
    lblEmpty.setTextFill(Color.rgb(255, 255, 255));

    // Set the password label to 'opened tab' color
    lblPassword.setTextFill(Color.rgb(80, 170, 255));

    // Enable empty tab components
    disableEmptyTabComponents();
  }

  /**
   * Initialize GPT. Set the tokens and create a new instance of GPT request.
   *
   * <p>Note: I would love to be able to name this method 'initializeGPT'. Unfortunately, we are not
   * allowed to have acronyms as method names as per the naming convention.
   */
  private void initializeChat() {
    // initialize an instance of GPT request
    gptRequest = new ChatCompletionRequest();

    // set the 'n' parameter for the request -> has to be '1'
    gptRequest.setN(1);

    // set the temperature for the request -> [0,2]
    gptRequest.setTemperature(1.4);

    // set the 'top p' value for the request -> [0,1]
    gptRequest.setTopP(0.5);

    // set the max tokens -> has to be at least '1'
    gptRequest.setMaxTokens(100);
  }

  /**
   * Initialize pseudocode instances. The method should get a 'random' pseudocode each round.
   *
   * @throws Exception thrown when there is an error initializing pseudocode instances.
   */
  private void initializePseudocode() throws Exception {
    // Get a random pseudo code
    psuedocodeIndex = (int) (Math.random() * (GameState.maxPseudocodes));

    System.out.println("Current psuedocode: " + psuedocodeIndex);

    // Hint index is initially zero
    hintIndex = 0;

    // Initialize the sequence
    intializeSequence();

    // Initialize the description and algorithm
    initializeDescription();
    initializeAlgorithm();

    // Append the description to the text area
    taDescription.appendText(description);

    // Create labels and panes for the algorithm
    setAlgorithm(algorithm);

    // Get the pseudocode in string form
    pseudocode = description + algorithm;
  }

  /**
   * Get the string sequence for the corresponding random pseudocode index.
   *
   * @return the string value of the sequence.
   * @throws Exception throw when class or field name is not found.
   */
  private void intializeSequence() throws Exception {
    // get the field name for the corresponding random pseudocode index
    String fieldName = "sequence" + Integer.toString(psuedocodeIndex);

    // create an object of 'Sequence'
    Sequence sequence = new Sequence();

    // create a runtime reference to the class, 'Sequence'
    Class<?> cls = sequence.getClass();

    // get the field for the corresponding field name
    Field fld = cls.getField(fieldName);

    // retrieve the object value from the field and cast it to string
    this.sequence = (String) fld.get(sequence);
  }

  /**
   * Get the string algorithm code snippet for the corresponding random pseudocode index.
   *
   * @return the string value of the algorithm code snippet.
   * @throws Exception throw when class or field name is not found.
   */
  private void initializeAlgorithm() throws Exception {
    // get the field name for the corresponding random pseudocode index
    String fieldName = "algorithm" + Integer.toString(psuedocodeIndex);

    // create an object of 'Algorithm'
    Algorithm algorithm = new Algorithm();

    // create a runtime reference to the class, 'Algorithm'
    Class<?> cls = algorithm.getClass();

    // get the field for the corresponding field name
    Field fld = cls.getField(fieldName);

    // retrieve the object value from the field and cast it to string
    this.algorithm = (String) fld.get(algorithm);
  }

  /**
   * Get the string description for the corresponding random pseudocode index.
   *
   * @return the string value of the description.
   * @throws Exception throw when class or field name is not found.
   */
  private void initializeDescription() throws Exception {
    // get the field name for the corresponding random pseudocode index
    String fieldName = "description" + Integer.toString(psuedocodeIndex);

    // create an object of 'Description'
    Description description = new Description();

    // create a runtime reference to the class, 'Description'
    Class<?> cls = description.getClass();

    // get the field for the corresponding field name
    Field fld = cls.getField(fieldName);

    // retrieve the object value from the field and cast it to string
    this.description = (String) fld.get(description);
  }

  /**
   * Generate a response from GPT.
   *
   * @param entityMessage the chat message to be sent to GPT.
   */
  private void getChatResponse(ChatMessage entityMessage, boolean isHint) {
    // add user input to GPT's user input history
    gptRequest.addMessage(entityMessage);

    // create a concurrent task for handling GPT response
    Task<Void> gptTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // Set GPT's response
            setChatResponse();
            return null;
          }
        };

    // If the task is running, disable certain components
    gptTask.setOnRunning(
        event -> {
          disableComponents();
        });

    // If the task succeeds, then enable components
    gptTask.setOnSucceeded(
        event -> {
          enableComponents();
        });

    // If the task fails, then enable components
    gptTask.setOnFailed(
        event -> {
          enableComponents();
        });

    // Create a thread to handle GPT concurrency
    Thread gptThread = new Thread(gptTask);

    // Start the thread
    gptThread.start();
  }

  /** Generate a GPT response. GPT should give a hint for the current pseudocode. */
  private void getUserHint() {
    // Initialize a new instance of gpt
    initializeChat();

    // Update the hint counter
    HintManager.updateHintCounter();

    // Get the incorrect line number for the following pseudocode and hint index
    int lineNumber = Integer.valueOf(sequence.charAt(hintIndex));

    // Get the hint prompt for GPT to analyze and generate a response
    String hint = GptPromptEngineering.getDecryptionHint(pseudocode, lineNumber);

    // Initialize a user hint message compatible for GPT to analyze
    ChatMessage userHintMessage = new ChatMessage("assistant", hint);

    // Get GPT's response
    getChatResponse(userHintMessage, true);

    // Update the hint index
    hintIndex = (hintIndex + 1) % GameState.maxSequence;
  }

  public double getMemoryUsed() {
    // Get the ratio of memory used
    memoryUsed = (memoryUsed / 75) * 32;

    // Get the memory used to 2 decimal places
    String roundedMemoryUsed = String.format("%.2f", memoryUsed);

    return Double.parseDouble(roundedMemoryUsed);
  }

  public Color getRandomColor() {
    Color green = Color.rgb(130, 240, 130);
    Color gray = Color.rgb(56, 57, 63);

    boolean isColorGreen = Math.random() < 0.5f;

    if (isColorGreen) {
      memoryUsed++;
    }

    return (isColorGreen ? green : gray);
  }

  /**
   * Set the chat response from GPT. This includes printing the response to the text area.
   *
   * @throws Exception thrown when we fail to retrieve a response from GPT.
   */
  private void setChatResponse() throws Exception {
    // Get GPT's response
    ChatCompletionResult gptResult = gptRequest.execute();

    // Get GPT's choice
    Choice gptChoice = gptResult.getChoices().iterator().next();

    // Get GPT's chat message
    ChatMessage gptMessage = gptChoice.getChatMessage();

    // Get the content of gpt's message in the form of a string
    String gptOutput = gptMessage.getContent();

    // Append the result to the text area
    taChat.appendText("ai> " + gptOutput + "\n\n");

    // Make text-to-speech read GPT's output
    tts.speak(gptOutput, AppUi.DECRYPTION);
  }

  private void setAlgorithm(String algorithm) {}

  private void setMemoryLocation(double x, double y, double w, double h) {
    // Initialize the offsets
    double horizontalOffset = 5;
    double verticalOffset = 85;

    // Intialize the padding
    double padding = 5;

    // Create a rectangle component
    Rectangle memory = new Rectangle(w, h);

    // Get a random color
    Color color = getRandomColor();

    // Set the location of the rectangle
    memory.setTranslateX((x * (w + padding)) + horizontalOffset);
    memory.setTranslateY((y * (h + padding)) + verticalOffset);

    // Set the color of the rectangle
    memory.setFill(color);

    // Set the style of the rectangle
    setRectangleStyle(memory);

    // Add the rectangle to the scene
    paDecryption.getChildren().add(memory);
  }

  private void setRectangleStyle(Rectangle rectangle) {
    // Initialize a glow for the rectangle
    Glow glow = new Glow();
    glow.setLevel(0.7);

    // Set the glow effect for the rectangle
    rectangle.setEffect(glow);
  }

  private void setPaneEntered(Pane pane) {
    pane.setStyle("-fx-background-color: rgb(29,30,37);");
  }

  private void setPaneExited(Pane pane) {
    pane.setStyle("-fx-background-color: rgb(20,20,23);");
  }

  private void setPolygonEntered(Polygon polygon) {
    polygon.setStroke(Color.rgb(29, 30, 37));
    polygon.setFill(Color.rgb(29, 30, 37));
  }

  private void setPolygonExited(Polygon polygon) {
    polygon.setStroke(Color.rgb(20, 20, 23));
    polygon.setFill(Color.rgb(20, 20, 23));
  }

  /** Enable components when a task is finished. */
  private void enableComponents() {
    // Enable the hint pane
    paHint.setDisable(false);
  }

  /** Enable empty tab components. */
  private void enableEmptyTabComponents() {
    // Set entered color for the pane
    setPaneEntered(paEmpty);

    // Set entered colors for the polygons
    setPolygonEntered(pgEmptyLeft);
    setPolygonEntered(pgEmptyRight);
  }

  /** Enable password tab components. */
  private void enablePasswordTabComponents() {
    // Set entered color for the pane
    setPaneEntered(paPassword);

    // Set entered colors for the polygons
    setPolygonEntered(pgPasswordLeft);
    setPolygonEntered(pgPasswordRight);
  }

  /** disable components when a task is running. */
  private void disableComponents() {
    // Disable the hint pane
    paHint.setDisable(true);
  }

  /** Disable empty tab components. */
  private void disableEmptyTabComponents() {
    // Set exited color for the pane
    setPaneExited(paEmpty);

    // Set exited colors for the polygons
    setPolygonExited(pgEmptyLeft);
    setPolygonExited(pgEmptyRight);
  }

  /** Disable password tab components. */
  private void disablePasswordTabComponents() {
    // Set exited color for the pane
    setPaneExited(paPassword);

    // Set exited colors for the polygons
    setPolygonExited(pgPasswordLeft);
    setPolygonExited(pgPasswordRight);
  }
}
