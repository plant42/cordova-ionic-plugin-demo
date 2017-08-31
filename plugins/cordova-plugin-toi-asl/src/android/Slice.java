/**
 * Created by steinar on 04.08.17.
 */
public class SlidingWindow {
  //Characteristics for one sliding window length of the bufferr
  private final Characteristic pace;
  private final Characteristic pitch;
  private final Characteristic volume;

  public SlidingWindow() {
    this.pace = new Characteristic();
    this.pitch = new Characteristic();
    this.volume = new Characteristic();
  }

  public Characteristic getPace() {
    return pace;
  }

  public Characteristic getPitch() {
    return pitch;
  }

  public Characteristic getVolume() {
    return volume;
  }
}
