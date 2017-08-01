import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
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
import android.content.pm.PackageManager;
import android.Manifest;


import java.util.*;
import java.lang.Math;
import be.tarsos.dsp.*;


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

  public static final String RECORD = Manifest.permission.RECORD_AUDIO;
  public static final int AUDIO_REQ_CODE = 0;

  AudioDispatcher dispatcher;
  Thread thread;
  long samples;

  String action;
  JSONArray args;
  CallbackContext callbackContext;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing "+TAG);
    timer = null;
    samples = 0;
  }

  private boolean internal_execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException{
    if (action.equals("echo")) {
      String phrase = args.getString(0);
      Log.d(TAG, phrase);
    }

    else if (action.equals("startStream")) {

      dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

      PitchDetectionHandler pdh = new PitchDetectionHandler() {
        @Override
        public void handlePitch(PitchDetectionResult result, AudioEvent e) {
          final float f = result.getPitch();
          synchronized (this){
            pitchAverage.add(f);
          }
        }
      };
      AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
      dispatcher.addAudioProcessor(p);
      thread = new Thread(dispatcher,"Audio Dispatcher");
      thread.start();


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
      }, 0, 250);
    }

    else if (action.equals("stopStream")){
      synchronized(this){
        if (timer != null){
          timer.cancel();
        }
        timer = null;
        if (dispatcher != null) {
          dispatcher.stop();
        }
        dispatcher = null;
        try{
          thread.join();
        } catch (Exception e){
          e.printStackTrace();
        }
      }
    }

    //Fix clear: Currently seems to do nothing.
    else if (action.equals("clearLog")){
      Log.d(TAG, "Clearing AudioStatLogger arraylists");
      pace.clear();
      pitchAverage.clear(); pitchModulation.clear(); pitchRange.clear();
      volumeAverage.clear(); volumeRange.clear(); volumeModulation.clear();
      samples=0;

    }
    return true;
  }

  public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(!cordova.hasPermission(RECORD))
    {
      this.action = action;
      this.args = args;
      this.callbackContext = callbackContext;
      getReadPermission(AUDIO_REQ_CODE);
      return false;
    }
    else {
      return internal_execute(action, args, callbackContext);
    }
  }

  protected void getReadPermission(int requestCode)
  {
    cordova.requestPermission(this, requestCode, RECORD);
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException
  {
    for(int r:grantResults)
    {
      if(r == PackageManager.PERMISSION_DENIED)
      {
        this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "PERMISSION_DENIED_ERROR"));
        return;
      }
    }

    internal_execute(this.action, this.args, this.callbackContext);
  }



  public void logStats(JSONArray args, final CallbackContext callbackContext) throws JSONException{
    // (Currently fake random placeholder data)
    Random rand = new Random();
    pace.add((float)rand.nextInt(101));

    //pitchAverage.add((float)rand.nextInt(101));
    /*
    pitchModulation.add((float)rand.nextInt(101));
    pitchRange.add((float)rand.nextInt(101));

    volumeAverage.add((float)rand.nextInt(101));
    volumeRange.add((float)rand.nextInt(101));
    volumeModulation.add((float)rand.nextInt(101));
    */
  }


  public void getStatSheet(JSONArray args, final CallbackContext callbackContext) throws JSONException{
    //Callbacks a JSON object containing a getStats set for every single characteristic
    long startTime = System.nanoTime();
    JSONObject statSheet = new JSONObject();
    statSheet.put("pace", getStats(pace, 5));

    statSheet.put("pitchAverage", getStats(pitchAverage, 20));
    /*statSheet.put("pitchRange", getStats(pitchRange));
    statSheet.put("pitchModulation", getStats(pitchModulation));

    statSheet.put("volumeAverage", getStats(volumeAverage));
    statSheet.put("volumeRange", getStats(volumeRange));
    statSheet.put("volumeModulation", getStats(volumeModulation));*/

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

  private JSONObject getStats(List<Float> statLog, int from, int to) throws JSONException{
    Log.d(TAG, "getStats called with from "+from+" to "+to);
    return getStats(statLog.subList(from, to));
  }
  private JSONObject getStats(List<Float> statLog, int nLast) throws JSONException{
    Log.d(TAG, "getStats called with nLast "+nLast);
    if (nLast > statLog.size()) nLast = statLog.size();
    Log.d(TAG, "Making nList sublist from "+(statLog.size()-nLast)+" to "+statLog.size());
    return getStats(statLog.subList(statLog.size()-nLast, statLog.size()));
  }

  private JSONObject getStats(List<Float> values) throws JSONException{
    if (values == null || values.size() == 0) return null;
    int N = values.size();

    Log.d(TAG, "Fetching stats on list with size "+values.size());

    float min=Float.MAX_VALUE;
    float max=Float.MIN_VALUE;
    float sum = 0, current = 0;

    for (float f: values){
      if (f == -1) continue;// Skip if no audio data
      sum += f;
      if (f < min) min = f; // New min found
      if (f > max) max = f; // New max found
    }
    float mean = sum/N;

    float diffSquareSum = 0;
    for (float f: values){
      if (f == -1) continue;// Skip if no audio data
      diffSquareSum += (f-mean)*(f-mean);
    }
    float standardDeviation = (float) Math.sqrt(diffSquareSum / N);

    JSONObject stats = new JSONObject();
    stats.put("now", values.get(N-1)); //Default <to> is values.size()-1, being last result recorded.
    stats.put("min", min);
    stats.put("max", max);
    stats.put("range", max-min);
    stats.put("mean", mean);
    stats.put("sd", standardDeviation);
    stats.put("median", median(values));
    return stats;
  }

  //TODO: Fix median. Currently doesn't return correct number.
  private float median(List<Float> values){
    List<Float> buffer = new ArrayList<Float>(values); //clone
    Collections.sort(buffer);
    int N = buffer.size();

    if (N % 2 == 0){ //[0][1][2][3] Two middle values, so average [1] and [2]
      float middleLeft = buffer.get((N/2)-1);
      float middleRight = buffer.get(N/2);
      return middleLeft+middleRight/2;
    } else { //[0][1][2]. Select middle [1].
      return buffer.get(N/2);
    }
  }

  private String freqToNote(float f){
    if(f >= 110 && f < 123.47) return "A";
    else if(f >= 123.47 && f < 130.81) return "B";
    else if(f >= 130.81 && f < 146.83) return "C";
    else if(f >= 146.83 && f < 164.81) return "D";
    else if(f >= 164.81 && f < 174.60) return "E";
    else if(f >= 174.61 && f < 185) return "F";
    else if(f >= 185.00 && f < 196) return "G";
    return "#";
  }
}
