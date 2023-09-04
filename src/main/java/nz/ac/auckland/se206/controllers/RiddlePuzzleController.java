package nz.ac.auckland.se206.controllers;

import java.io.IOException;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.constants.GameState;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;
import nz.ac.auckland.se206.utilities.Timer;

/** Controller class for the chat view. */
public class RiddlePuzzleController {
  @FXML private TextArea taChat;
  @FXML private Button btnAnswer1;
  @FXML private Button btnAnswer2;
  @FXML private Button btnAnswer3;
  @FXML private Button btnNavigate;
  @FXML private Label lblTime;

  private ChatCompletionRequest chatCompletionRequest;
  private String answer1;
  private String answer2;
  private String answer3;
  private StringProperty answer1Property = new SimpleStringProperty();
  private StringProperty answer2Property = new SimpleStringProperty();
  private StringProperty answer3Property = new SimpleStringProperty();
  private StringProperty navigateProperty = new SimpleStringProperty();

  /**
   * Initializes the chat view, loading the riddle.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {

    btnAnswer1.textProperty().bind(answer1Property);
    btnAnswer2.textProperty().bind(answer2Property);
    btnAnswer3.textProperty().bind(answer3Property);
    btnNavigate.textProperty().bind(navigateProperty);
    navigateProperty.set("Go Back");

    updateScene();
    loadRiddle();
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    taChat.appendText(msg.getRole() + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Loads a riddle from the GPT model.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private void loadRiddle() {
    // Generate a random number between 0 and 9
    int randomNumber = (int) (Math.random() * 10);

    // Select a concept from the list of concepts
    String[] concepts = {
      "Ethics",
      "Racial Profiling",
      "Privacy",
      "Bias",
      "Consent",
      "Empathy",
      "Sustainability",
      "Human Rights",
      "Justice",
      "Equality"
    };
    String concept = concepts[randomNumber];

    // If this is the first riddle, introduce the puzzle
    if (GameState.riddlesSolved == 0) {
      ChatMessage intro =
          new ChatMessage(
              "assistant",
              "Please help me solve these 3 riddles to update the vocabulary in my programming!");
      appendChatMessage(intro);
    }

    // Generate a loading message
    ChatMessage loading =
        new ChatMessage(
            "assistant", "Generating riddle " + (GameState.riddlesSolved + 1) + " of 3...");
    appendChatMessage(loading);

    // Create a new thread to run the GPT model
    Task<Void> generateRiddle =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            System.out.println("Task.call method: " + Thread.currentThread().getName());
            // Create a new chat completion request
            chatCompletionRequest =
                new ChatCompletionRequest()
                    .setN(1)
                    .setTemperature(0.2)
                    .setTopP(0.5)
                    .setMaxTokens(100);
            ChatMessage gptResponse =
                runGpt(new ChatMessage("user", GptPromptEngineering.getRiddlePuzzle(concept)));
            System.out.println(gptResponse.getContent());

            // If the response is from the assistant, process the output
            if (gptResponse != null && gptResponse.getRole().equals("assistant")) {
              processGptOutputForButtons(gptResponse.getContent(), concept);
            } else {
              // If GPT does not provide options in the correct format, generate them manually
              answer1 = concept;
              answer2 = concepts[(randomNumber + 1) % 10];
              answer3 = concepts[(randomNumber + 2) % 10];
            }
            // Update the UI thread
            Platform.runLater(
                () -> {
                  answer1Property.set(answer1);
                  answer2Property.set(answer2);
                  answer3Property.set(answer3);
                  btnAnswer1.setDisable(false);
                  btnAnswer2.setDisable(false);
                  btnAnswer3.setDisable(false);
                });
            return null;
          }

          /*
           * Notify if the API call succeeded
           */
          @Override
          protected void succeeded() {
            super.succeeded();
            System.out.println("Successfully loaded");
          }

          /*
           * Notify if the API call failed
           */
          @Override
          protected void failed() {
            super.failed();
            System.out.println("Failed to load");
          }
        };

    // Start the thread
    Thread newThread = new Thread(generateRiddle, "New Thread");
    newThread.start();
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    // Send the message to the GPT model
    try {
      ChatMessage riddle = null;
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());
      // Format the ridddle correctly
      if (result.getChatMessage().getRole().equals("assistant")
          && result.getChatMessage().getContent().startsWith("Riddle:")) {
        if (result.getChatMessage().getContent().indexOf('^') != 1) {
          riddle =
              new ChatMessage(
                  "assistant",
                  result
                      .getChatMessage()
                      .getContent()
                      .substring(
                          result.getChatMessage().getContent().indexOf(':') + 1,
                          result.getChatMessage().getContent().indexOf('^')));
        }
        appendChatMessage(riddle);
      } else {
        appendChatMessage(result.getChatMessage());
      }
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      // TODO handle exception appropriately
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Processes the output from the GPT model to generate the buttons.
   *
   * @param gptOutput the output from the GPT model
   * @param concept the concept to be used in the riddle
   */
  private void processGptOutputForButtons(String gptOutput, String concept) {
    String[] segments = gptOutput.split("\\}");
    int randomNumber = (int) (Math.random() * 3);
    boolean conceptPresent = false;

    // Check if the concept is already present in the options
    for (int i = 0; i < segments.length; i++) {
      if (segments[i].contains(concept)) {
        conceptPresent = true;
      }
    }

    // If the concept is not present, add it to the options
    if (!conceptPresent) {
      segments[randomNumber] = concept;
    }

    // Change the answer options to the options generated by GPT
    if (segments.length >= 2) {
      answer1 =
          segments[(0 + randomNumber) % 3].substring(
              segments[(0 + randomNumber) % 3].lastIndexOf("{") + 1);
      answer2 =
          segments[(1 + randomNumber) % 3].substring(
              segments[(1 + randomNumber) % 3].lastIndexOf("{") + 1);
      answer3 =
          segments[(2 + randomNumber) % 3].substring(
              segments[(2 + randomNumber) % 3].lastIndexOf("{") + 1);
    }
  }

  /*
   * Handle input buttion option clicks
   *
   * @param event the action event triggered by the button click
   */
  @FXML
  private void onButtonClicked(ActionEvent event) throws ApiProxyException {
    Button clickedButton = (Button) event.getSource();
    // Get the text from the button
    String buttonText = clickedButton.getText();

    // Disable all buttons and the navigate button when a button has been clicked
    btnAnswer1.setDisable(true);
    btnAnswer2.setDisable(true);
    btnAnswer3.setDisable(true);
    btnNavigate.setDisable(true);

    // Generate a loading message
    ChatMessage loading = new ChatMessage("assistant", "Analysing your input...");
    appendChatMessage(loading);

    // Create a new thread to run the GPT model
    Task<Void> buttonClickTask =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            // Send the button text as a response to GPT
            ChatMessage responseMsg = runGpt(new ChatMessage("user", buttonText));

            // Update UI based on the response
            Platform.runLater(
                () -> {
                  // If the response is from the assistant and the answer is correct, update the
                  // number of riddles solved
                  if (responseMsg.getRole().equals("assistant")
                      && responseMsg
                          .getContent()
                          .startsWith("Yes! That sounds right with my programming!")) {
                    GameState.riddlesSolved++;
                    if (GameState.riddlesSolved == 1 || GameState.riddlesSolved == 2) {
                      navigateProperty.set("Next Riddle");
                    }
                    // If all riddles are solved, update the navigate button text and thank the
                    // player
                    if (GameState.riddlesSolved == 3) {
                      navigateProperty.set("Exit Puzzle");
                      ChatMessage outro =
                          new ChatMessage(
                              "assistant",
                              "That is three riddles solved! Thank you for helping recalibrate my"
                                  + " drives.");
                      appendChatMessage(outro);
                    }
                    // Set the navigate button to be enabled if the riddle is solved
                    btnNavigate.setDisable(false);
                  } else {
                    // If the answer is incorrect, enable the input buttons again
                    btnAnswer1.setDisable(false);
                    btnAnswer2.setDisable(false);
                    btnAnswer3.setDisable(false);
                    if (GameState.riddlesSolved != 0) {
                      btnNavigate.setDisable(true);
                    } else {
                      btnNavigate.setDisable(false);
                    }
                  }
                });

            return null;
          }
        };

    // Start the thread
    Thread newThread = new Thread(buttonClickTask, "Button Click Thread");
    newThread.start();
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onNavigateButton(ActionEvent event) throws ApiProxyException, IOException {
    if (GameState.riddlesSolved == 0) {
      // If the riddle is not solved, navigate back to the office
      App.setUi(AppUi.OFFICE);
    } else if (GameState.riddlesSolved == 1 || GameState.riddlesSolved == 2) {
      // If the riddle is solved, load the next riddle and disable the buttons whilst the riddle is
      // loading
      loadRiddle();
      btnNavigate.setDisable(true);
      btnAnswer1.setDisable(true);
      btnAnswer2.setDisable(true);
      btnAnswer3.setDisable(true);
    } else if (GameState.riddlesSolved == 3) {
      // If all riddles are solved, navigate back to the office
      App.setUi(AppUi.OFFICE);
      GameState.isRiddleResolved = true;
    }
  }

  /**
   * Update all things related to timing here. Such an example is using animation timer to update
   * the timer text on each frame.
   */
  private void updateScene() {
    AnimationTimer animationTimer =
        new AnimationTimer() {
          @Override
          public void handle(long time) {
            lblTime.setText(Timer.getTime());
          }
        };

    animationTimer.start();
  }
}