import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
import java.util.Date;
import java.util.Timer;
import java.util.Random;
import java.util.TimerTask;

import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;




public class AudioStatLogger extends CordovaPlugin {
  private static final String TAG = "AudioStatLogger";
  private Timer timer;
  JSONObject sample;

  public ArrayList<Float> pace = new ArrayList<Float>();

  public ArrayList<Float> pitchAverage = new ArrayList<Float>();
  public ArrayList<Float> pitchModulation = new ArrayList<Float>();
  public ArrayList<Float> pitchRange = new ArrayList<Float>();

  public ArrayList<Float> volumeAverage = new ArrayList<Float>();
  public ArrayList<Float> volumeRange = new ArrayList<Float>();
  public ArrayList<Float> volumeModulation = new ArrayList<Float>();

  long samples;

  //Characteristics and their named stored as in JSON file.
  //ArrayList<Float>[] charLists;
  //charLists = new ArrayList<Float>[]{pace, pitchAverage, pitchModulation, pitchRange, volumeAverage, volumeRange, volumeModulation};
  //charLists = {pace, pitchAverage, pitchModulation, pitchRange, volumeAverage, volumeRange, volumeModulation};
  //String[] charNames = {"pace", "pitchAverage", "pitchModulation", "pitchRange", "volumeAverage", "volumeRange", "volumeModulation"};

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Initializing "+TAG);
    timer = null;
    samples = 0;
  }

  public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (action.equals("echo")) {
      String phrase = args.getString(0);
      Log.d(TAG, phrase);
    }

    else if (action.equals("startStream")) {
      if (timer != null) return false; //Only want one timertask running.
      timer = new Timer();
      timer.scheduleAtFixedRate(new TimerTask() {
        public void run() {
          try {
            logStats(args, callbackContext);
            getStatSheet(args, callbackContext);
          } catch (JSONException exception) {
            exception.printStackTrace();
          }
        }
      }, 0, 1000);
    }

    else if (action.equals("stopStream")){
      timer.cancel();
      timer = null;
    }

    //Fix clear: Currently seems to do nothing.
    else if (action.equals("clearLog")){
      Log.d(TAG, "clearLog action triggered in AudioStatLogger.java");
      pace.clear();
      pitchAverage.clear(); pitchModulation.clear(); pitchRange.clear();
      volumeAverage.clear(); volumeRange.clear(); volumeModulation.clear();

    }

    return true;
  }


  public void logStats(JSONArray args, final CallbackContext callbackContext) throws JSONException{
    // (Currently fake random placeholder data)
    Random rand = new Random();
    pace.add((float)rand.nextInt(101));

    pitchAverage.add((float)rand.nextInt(101));
    pitchModulation.add((float)rand.nextInt(101));
    pitchRange.add((float)rand.nextInt(101));

    volumeAverage.add((float)rand.nextInt(101));
    volumeRange.add((float)rand.nextInt(101));
    volumeModulation.add((float)rand.nextInt(101));
  }


  public void getStatSheet(JSONArray args, final CallbackContext callbackContext) throws JSONException{
    //Callbacks a JSON object containing a getStats set for every single characteristic
    long startTime = System.nanoTime();
    JSONObject statSheet = new JSONObject();
    statSheet.put("pace", getStats(pace, 5));

    statSheet.put("pitchAverage", getStats(pitchAverage));
    statSheet.put("pitchRange", getStats(pitchRange));
    statSheet.put("pitchModulation", getStats(pitchModulation));

    statSheet.put("volumeAverage", getStats(volumeAverage));
    statSheet.put("volumeRange", getStats(volumeRange));
    statSheet.put("volumeModulation", getStats(volumeModulation));

    statSheet.put("samples", samples++);
    statSheet.put("finishTime", (System.nanoTime() - startTime) / 1000000.0);
    //finishTime revealed ~3ms of computation time is added for every 60 statSheets added.

    final PluginResult result = new PluginResult(PluginResult.Status.OK, statSheet);
    result.setKeepCallback(true);
    callbackContext.sendPluginResult(result);
  }


  /* getStats(values) is computed using all values
   * getStats(values, n) is computed using only the <n> last results
   * getStats(values, from, to) is computed using the stats between index #<from> and <to>
   */

  private JSONObject getStats(ArrayList<Float> values) throws JSONException{
    return getStats(values, 0, values.size()-1); //If no more args, do entire thing.
  }
  private JSONObject getStats(ArrayList<Float> values, int nLast) throws JSONException{
    Log.d(TAG, "Trying to get stats between index "+(values.size()-nLast)+" and "+(values.size()-1));
    return getStats(values, values.size()-nLast, values.size()-1);
  }
  private JSONObject getStats(ArrayList<Float> values, int from, int to) throws JSONException{
    if (values == null || values.size() == 0) return null;
    int size = values.size();
    if (from < 0) from = 0;
    if (to >= size) to = size-1;
    Log.d(TAG, "Will now access arraylist indexes between "+to+" and "+from);

    float min=Float.MAX_VALUE;
    float max=Float.MIN_VALUE;
    float sum = 0, current = 0;

    // TODO: Check whether this loop requires i<=to
    for (int i=from; i < to; i++){
      current = values.get(i);
      sum += current;
      if (current < min) min = current; //New min
      if (current > max) max = current; //New max
    }
    float mean = sum/(1+to-from);

    JSONObject stats = new JSONObject();
    stats.put("now", values.get(to)); //Default <to> is values.size()-1, being last result recorded.
    stats.put("min", min);
    stats.put("max", max);
    stats.put("range", max-min);
    stats.put("mean", mean);
    //stats.put("sd", standardDeviation(values, from, to, mean));
    //stats.put("median", median(values, from, to));
    return stats;
  }

  private float standardDeviation(ArrayList<Float> values, int from, int to, float mean){
    float[] buffer = new float[1+to-from];
    float sum = 0;
    // TODO: Check whether this loop requires i<=to
    for (int i=from; i<to; i++){
      sum += (buffer[i] = (values.get(i) - mean)*(values.get(i) - mean)); //(val - mean)^2
    }
    return (float)Math.sqrt(sum/(1+to-from));
  }

  //TODO: Fix median. Currently doesn't return correct number.
  private float median(ArrayList<Float> values, int from, int to){
    ArrayList<Float> buffer = (ArrayList<Float>)values.clone();
    Collections.sort(buffer);
    int size = buffer.size();

    if (size % 2 == 0){ //[0][1][2][3] Two middle values, so average [1] and [2]
      float middleLeft = buffer.get((size/2)-1);
      float middleRight = buffer.get(size/2);
      return middleLeft+middleRight/2;
    } else { //[0][1][2]. Select middle [1].
      return buffer.get(size/2);
    }
  }
}
