package nz.ac.auckland.se206.controllers.menus;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.constants.GameState;
import nz.ac.auckland.se206.controllers.puzzles.LogicGatePuzzleController;
import nz.ac.auckland.se206.utilities.Timer;

public class WinningScreenController {
  @FXML private Button btnPlayAgain;
  @FXML private Button btnMainMenu;
  @FXML private Button btnViewGameStats;

  /** Initialize the controller. */
  @FXML
  private void initialize() {}

  /*
   * This method will reset all the global fields in GameState
   *
   */
  private void resetGlobalVariables() {
    //
    GameState.isKeyFound = false;
    GameState.isRiddleResolved = false;
    GameState.riddlesSolved = 0;
  }

  /*
   * This method will restart the game
   */
  @FXML
  private void onPlayAgain() {

    resetGlobalVariables();
    LogicGatePuzzleController.logicGateRestart();

    // set the timer's countdown time
    Timer.setTime(GameState.maxTime);

    // start the timer
    Timer.play();

    // change scene to Office
    App.setUi(AppUi.OFFICE);
  }

  /*
   * This method will show the game stats
   */
  @FXML
  private void onViewGameStats() {}

  /*
   * This method will set the scene to the main menu
   */
  @FXML
  private void onBackToMenu() {
    App.setUi(AppUi.MENU);
  }
}