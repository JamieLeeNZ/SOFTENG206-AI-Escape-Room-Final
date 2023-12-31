package nz.ac.auckland.se206.utilities;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.constants.GameState;

/**
 * This utility class contains a method to handle when key events are pressed by the player. These
 * key events are those that occur globally.
 */
public class KeyEventsHandler implements EventHandler<KeyEvent> {
  /**
   * Return a boolean value based on whether all the puzzles are solved.
   *
   * @return a boolean value based on whether all the puzzles are solved.
   */
  public static boolean isAllPuzzleSolved() {
    return GameState.isDecryptionSolved
        && GameState.isRiddleResolved
        && GameState.isLogicGateSolved;
  }

  @Override
  public void handle(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE && GameState.isDeveloperMode) {
      // Handle the Escape key press
      App.setUi(AppUi.MENU);
    }
  }
}
