import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Result {
  private int sliceCount;   // Number of slices used to compute
  private int silenceCount; // Number of slices that are silent
  private StatSheet pitch, volume;

  public Result(StatSheet pitch, StatSheet volume){
    this.pitch = pitch;
    this.volume = volume;

  }

  public StatSheet getPitchStats(){
    return pitch;
  }

  public StatSheet getVolumeStats(){
    return volume;
  }

  public int getSlices(){return sliceCount;}

  public int getSilences(){
    return silenceCount;
  }

  public void setSlices(int sliceCount){
    this.sliceCount = sliceCount;
  }

  public void setSilences(int sliceCount){
    this.silenceCount = silenceCount;
  }

  public void addSlices(int addCount){
    this.sliceCount += addCount;
  }

  public void addSilences(int addCount){
    this.silenceCount += addCount;
  }

  public JSONObject toJSON() throws JSONException{
    JSONObject json = new JSONObject();
    json.put(getVolumeStats().toJSON(), "volume");
    json.put(getPitchStats().toJSON(), "pitch");
    json.put(sliceCount, "sliceCount");
    json.put(silenceCount, "silenceCount");
    return json;
  }
}
