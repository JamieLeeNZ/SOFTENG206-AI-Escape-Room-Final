package nz.ac.auckland.se206.utilities;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/** The timer class keeps track of the time left for the player. */
public class Timer {
  private static Timeline timer;

  private static int minutes;
  private static int seconds;
  private static int time;

  /**
   * Create a new instance of timer.
   *
   * @param time the input time. This should be 2, 4, or 6 minutes only (in seconds).
   */
  public Timer(int time) {
    Timer.time = time;

    initialize();
  }

  /** Initializes the timer. This is called when a new instance of timer is created. */
  private static void initialize() {
    timer =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                event -> {
                  Timer.update();
                }));
  }

  /**
   * Return the string value of minutes left on the timer.
   *
   * @return String
   */
  public static String getMinutes() {
    return (minutes < 10 ? "0" + String.valueOf(minutes) : String.valueOf(minutes));
  }

  /**
   * Return the string value of seconds left on the timer.
   *
   * @return String
   */
  public static String getSeconds() {
    return (seconds < 10 ? "0" + String.valueOf(seconds) : String.valueOf(seconds));
  }

  /**
   * Return a the time in form of a string. The formatting is as follows: mm:ss. So for example, 1
   * minute and 22 seconds would look like: 01:22.
   *
   * @return String
   */
  public static String getTime() {
    return getMinutes() + ":" + getSeconds();
  }

  /**
   * Returns the integer value of the time left (in seconds).
   *
   * @return int
   */
  public static int getIntegerTime() {
    return time;
  }

  /** Updates the timer. This method is called inside of the initialize method. */
  public static void update() {
    time -= 1;
    minutes = time / 60;
    seconds = time % 60;
  }

  /**
   * Return a boolean value based on whether the timer has reached zero or not.
   *
   * @return boolean
   */
  public static boolean checkTime() {
    return time == 0;
  }

  /** Start the timer. */
  public static void play() {
    timer.setCycleCount(time);
    timer.play();
  }

  /** Pause or stop the timer. */
  public static void stop() {
    timer.stop();
  }
}