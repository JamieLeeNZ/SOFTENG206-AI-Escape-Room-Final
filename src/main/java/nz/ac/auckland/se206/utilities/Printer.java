package nz.ac.auckland.se206.utilities;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import nz.ac.auckland.se206.AudioManager;
import nz.ac.auckland.se206.constants.GameState;
import nz.ac.auckland.se206.constants.Instructions;

/**
 * This utility class contains a method to print text onto the text area, letter by letter, used
 * prominently in the terminal controller and decryption puzzle controller.
 */
public class Printer {
  private static Timeline printTextEvent;

  private static int currentLetterPosition;

  private static String currentMessage;

  /**
   * Prints the message onto the text area, letter by letter.
   *
   * @param textArea the passed in text area
   * @param message the message to be printed
   * @param speed the speed at which the letters will be printed
   */
  public static void printText(TextArea textArea, String message, double speed) {
    // Play the printing dialogue sound effect
    AudioManager.playDialogue();

    // Initialize pointers
    currentLetterPosition = 0;
    currentMessage = message;

    // initialize a timeline for the printing event
    printTextEvent = new Timeline();

    // create a keyframe for printing and add it to the timeline
    KeyFrame printTextKeyFrame =
        new KeyFrame(
            Duration.seconds(speed),
            event -> {
              // get the current letter of the string to be appended to the text area
              char currentCharacter = message.charAt(currentLetterPosition);
              String currentLetter = String.valueOf(currentCharacter);

              // append the current letter to the text area
              textArea.appendText(currentLetter);

              currentLetterPosition++;
            });

    GameState.isPrinting = true;

    // add the printer keyframe to the printer timeline
    printTextEvent.getKeyFrames().addAll(printTextKeyFrame);

    // printing event will finish once the full message has been displayed
    printTextEvent.setCycleCount(message.length());
    printTextEvent.play();

    // let the program know the printing event is finished and remove the key frame
    printTextEvent.setOnFinished(
        e -> {
          GameState.isPrinting = false;
          printTextEvent.getKeyFrames().removeAll();
          AudioManager.stopDialogue();
        });
  }

  /** Stop the printing event. Most of the calls will be to cancel the printing event. */
  public static void stop() {
    GameState.isPrinting = false;
    printTextEvent.stop();
    AudioManager.stopDialogue();
  }

  /**
   * Method is called to retrieve the last printed position of the letter when the printing event is
   * CANCELLED.
   *
   * @return the letter position of the current message.
   */
  public static int getCurrentLetterPosition() {
    return currentLetterPosition;
  }

  /**
   * Method is most likely called when the printing is cancelled, otherwise, refer to the return
   * description.
   *
   * @return the current printing message or the previously printed message.
   */
  public static String getCurrentMessage() {
    return currentMessage;
  }

  /**
   * Print the puzzle solved message to the text area when the decryption puzzle is successfully
   * solved by the player.
   *
   * @param textArea the passed in text area
   * @param message the message to be printed
   */
  public static void printDecryptionPuzzleFinished(TextArea textArea, String message) {
    // Initialize a pause transition
    PauseTransition pause = new PauseTransition(Duration.seconds(2));

    // Print the final message on finish
    pause.setOnFinished(
        event -> {
          textArea.clear();
          printText(textArea, message, Instructions.printSpeed);
        });

    // Sleep for the duration specified
    pause.play();
  }
}
