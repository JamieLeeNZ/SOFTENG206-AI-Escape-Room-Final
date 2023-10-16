package nz.ac.auckland.se206.utilities;

import java.io.FileInputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Wire {
  private ImageView wire;

  private String onLogicColour = "00ff00"; // green
  private String offLogicColour = "ff0000"; // red

  private Image greenWire;
  private Image redWire;

  public Wire(ImageView wire) {
    this.wire = wire;

    try {
      this.greenWire =
          new Image(
              new FileInputStream(
                  "src/main/resources/images/BreakerRoom/LogicGatePuzzle/Wires/greenWire.png"));
      this.redWire =
          new Image(
              new FileInputStream(
                  "src/main/resources/images/BreakerRoom/LogicGatePuzzle/Wires/redWire.png"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    setBackground(false);
  }

  /**
   * Takes in colour to set background pane colour to
   *
   * @param colour
   */
  public void setBackground(Boolean colour) {
    // set background colour
    if (colour) {
      wire.setImage(greenWire);
    } else {
      wire.setImage(redWire);
    }
  }
}
