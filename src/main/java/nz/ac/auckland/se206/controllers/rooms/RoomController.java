package nz.ac.auckland.se206.controllers.rooms;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.ChatManager;
import nz.ac.auckland.se206.HintManager;
import nz.ac.auckland.se206.constants.GameState;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

public abstract class RoomController {
  @FXML private Pane paOffice;

  @FXML private Label lblTime;

  @FXML private Button btnHint;
  @FXML private Button btnLeft;
  @FXML private Button btnRight;

  @FXML private TextArea taChat;
  @FXML private TextField tfChat;
  @FXML private VBox vbChat;
  @FXML private Label lblAiChat;
  @FXML private Label lblPlayerChat;

  @FXML private Rectangle recOpaque;

  @FXML private ImageView imgAvatar;
  @FXML private ImageView imgAvatarShaddow;
  @FXML private ImageView imgEmotion;

  private static StringProperty aiChatProperty = new SimpleStringProperty();
  private static StringProperty playerChatProperty = new SimpleStringProperty();
  private static ChatCompletionRequest gptRequest;

  /** Initialize the general office. */
  @FXML
  protected void initialize() {
    lblAiChat.textProperty().bind(aiChatProperty);
    aiChatProperty.set(GameState.currentAiMessage);
    lblPlayerChat.textProperty().bind(playerChatProperty);
    playerChatProperty.set(GameState.currentPlayerMessage);
    initializeChat();
  }

  /**
   * Initialize GPT. Set the tokens and create a new instance of GPT request.
   *
   * <p>Note: I would love to be able to name this method 'initializeGPT'. Unfortunately, we are not
   * allowed to have acronyms as method names as per the naming convention.
   */
  public void initializeChat() {
    // initialize the chat message field
    ChatMessage gptMessage;

    // initialize GPT chat message object
    gptMessage = new ChatMessage("assistant", GptPromptEngineering.initializeBackStory());

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

    // get a response from GPT to setup the chat
    getChatResponse(gptMessage, false);
  }

  /**
   * Generate a response from GPT.
   *
   * @param entityMessage the chat message to be sent to GPT.
   */
  public void getChatResponse(ChatMessage entityMessage, boolean isHint) {
    // Disable all chat components
    disableChatComponents();

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
          startThinking();
        });

    // If the task succeeds, then enable components
    gptTask.setOnSucceeded(
        event -> {
          enableChatComponents();
          stopThinking();
        });

    // If the task fails
    gptTask.setOnFailed(
        event -> {
          enableChatComponents();
          stopThinking();
          System.out.println("FAILED TO GENERATE RESPONSE");
        });

    // Create a thread to handle GPT concurrency
    Thread gptThread = new Thread(gptTask);

    // Start the thread
    gptThread.start();
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

    // Add the message to GPT's context
    gptRequest.addMessage(gptMessage);

    // Update the AI chat label on the FX application thread
    Platform.runLater(
        () -> {
          GameState.currentAiMessage = gptOutput;
          aiChatProperty.set(GameState.currentAiMessage);
          swapLabelsOrder();
          lblAiChat.setVisible(true);
        });
  }

  /**
   * Update the chat area given the response was from the user.
   *
   * @param message the message to be appended to the text area.
   */
  public void setUserResponse(String message) {
    GameState.currentPlayerMessage = message;
    playerChatProperty.set(GameState.currentPlayerMessage);
    tfChat.clear();
    swapLabelsOrder();
    lblPlayerChat.setVisible(true);
  }

  @FXML
  protected void onAiClicked(MouseEvent event) {
    GameState.muted = GameState.muted == false;
    GameState.tts.stop();
    ChatManager.toggleAiMuted();
    GameState.isChatting = !GameState.isChatting;
    if (GameState.isChatting) {
      vbChat.setVisible(true);
      tfChat.setVisible(true);
      recOpaque.setVisible(true);
      if (GameState.currentAiMessage == "") {
        lblAiChat.setVisible(false);
      }
      if (GameState.currentPlayerMessage == "") {
        lblPlayerChat.setVisible(false);
      }
    } else {
      vbChat.setVisible(false);
      tfChat.setVisible(false);
      recOpaque.setVisible(false);
    }
  }

  public void startThinking() {
    tfChat.setDisable(true);
    tfChat.setOpacity(0.5);
  }

  public void stopThinking() {
    tfChat.setDisable(false);
    tfChat.setOpacity(1);
  }

  @FXML
  private void onMouseEnterAi(Event event) {
    // enter
    imgAvatarShaddow.setVisible(true);
  }

  @FXML
  private void onMouseExitAi(Event event) {
    // enter
    imgAvatarShaddow.setVisible(false);
  }

  /**
   * Check if there is a keyboard event. If there is a keyboard event, handle the event
   * appropriately.
   *
   * @param keyEvent this event is generated when a key is pressed, released, or typed
   */
  @FXML
  private void onKeyPressed(KeyEvent keyEvent) {
    String userInput = "";

    // get the user input from the chat text field
    if (keyEvent.getCode() == KeyCode.ENTER) {
      userInput = tfChat.getText();
    }

    // trim the user input
    userInput = userInput.trim();

    // check if the user input is empty
    if (userInput == null || userInput.isEmpty()) {
      return;
    }

    // initialize user chat message object
    ChatMessage userMessage;

    // create a new instance of user chat message object
    userMessage = new ChatMessage("user", userInput);

    setUserResponse(userInput);

    getChatResponse(userMessage, false);
  }

  /** Disable chat components. You can include the things you want to disable here. */
  private void disableChatComponents() {
    tfChat.setEditable(false);

    // Disable all hint buttonss
    HintManager.disableHints();
  }

  /** Enable chat components. You can include the things you want to enable here. */
  private void enableChatComponents() {
    tfChat.setEditable(true);

    // Enable all hint buttonss
    HintManager.enableHints();
  }

  // Method to swap the order of the labels
  private void swapLabelsOrder() {
    // Get the current index of lblAiChat
    int aiChatIndex = vbChat.getChildren().indexOf(lblAiChat);

    // Remove both labels from the VBox
    vbChat.getChildren().removeAll(lblAiChat, lblPlayerChat);

    // Swap the order of the labels
    if (aiChatIndex == 0) {
      vbChat.getChildren().addAll(lblPlayerChat, lblAiChat);
    } else {
      vbChat.getChildren().addAll(lblAiChat, lblPlayerChat);
    }
  }
}
