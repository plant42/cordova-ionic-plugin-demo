/**
 * Created by steinar on 09.08.17.
 */

import java.util.*;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


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
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.lang.Math;
import java.lang.Thread;

//Contains all the logged values for a recording
public class Timeline {
  private final List<Float> pitchList = Collections.synchronizedList(new ArrayList<Float>());
  private final List<Float> volumeList = Collections.synchronizedList(new ArrayList<Float>());

  public Timeline() {

  }

  public void addPitch(float pitch) {
    this.pitchList.add(pitch);
  }

  public void addVolume(float volume) {
    this.volumeList.add(volume);
  }

  private getStats(String ){

  }

  // Get stats using the subList(from, to) of list
  private Stats getStatsForSegment(List<Float> timeline, int from, int to) throws JSONException {
    Log.d(TAG, "getStats called with from " + from + " to " + to);
    List<Slice> selection = Collections.synchronizedList(new ArrayList<Slice>());
    selection = timeline.subList(from, to);
    return getStatsForAll(selection);
  }

  // Get stats using the <nLast> most recent slice(s) in list.
  private Stats getStatsForNLast(List<Slice> timeline, int nLast) throws JSONException {
    Log.d(TAG, "getStats called with nLast " + nLast);
    if (nLast > timeline.size()) nLast = timeline.size();
    List<Slice> selection = Collections.synchronizedList(new ArrayList<Slice>());
    selection = timeline.subList(timeline.size() - nLast, timeline.size());
    return getStatsForAll(selection);
  }

  // Get stats for all slices in list
  private Stats getStatsForAll(List<Slice> timeline) throws JSONException {

    Log.d(TAG, "JSON: " + selectionStats.toJSON());
    return selectionStats.toJSON();
  }


  //TODO: Redo for new data structure
  private float standardDeviation(List<Slice> timeline) {
    return -1;
    /*
    float diffSquareSum = 0;
    invalid = 0;

    for (Slice s : selection) {
      float v = selection.get(i);
      if (v < 0) {
        invalid++;
        continue; // Exclude if no audio data
      }
      diffSquareSum += (v - mean) * (v - mean);
    }
    float standardDeviation = (float) Math.sqrt(diffSquareSum / (N - invalid));
    */
  }

  //TODO: Redo for new data structure. Requires sorting.
  private float median(List<Slice> timeline) {
    //TODO: Check out stream(), arrayList.removeAll(), arrayList.removeRange()
    return -1;
    /*
    //Clone arrayList
    List<Float> buffer;
    buffer = new ArrayList<Float>(values); //Clone to modify

    //Remove all invalid entries
    Iterator<Float> it = buffer.iterator();
    while (it.hasNext()){
      Float f = it.next();
      if (f < 0){
        it.remove();
      }
    }

    //Sort buffer and exclude invalids
    Collections.sort(buffer);
    int firstValidIndex = 0; //
    for (int i=0; i<buffer.size()-1; i++){
      if (buffer.get(i) > 0) firstValidIndex = i;
    }
    buffer = buffer.subList(firstValidIndex, buffer.size()-1);

    //Return value in the middle of sorted list
    int N = buffer.size();
    if (N % 2 == 0) { //[0][1][2][3] Two middle values, so average [1] and [2]
      float middleLeft = buffer.get((N / 2) - 1);
      float middleRight = buffer.get(N / 2);
      return middleLeft + middleRight / 2;
    } else { //[0][1][2]. Select middle [1].
      return buffer.get(N / 2);
    }
    */
  }

  //TODO:
  private String freqToNote(float f) {
    //Probably isn't accurate, but could be need to view a pitch as musical note too.
    if (f >= 110 && f < 123.47) return "A";
    else if (f >= 123.47 && f < 130.81) return "B";
    else if (f >= 130.81 && f < 146.83) return "C";
    else if (f >= 146.83 && f < 164.81) return "D";
    else if (f >= 164.81 && f < 174.60) return "E";
    else if (f >= 174.61 && f < 185) return "F";
    else if (f >= 185.00 && f < 196) return "G";
    return "#";
  }
}
