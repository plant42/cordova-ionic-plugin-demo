// Created by steinar on 04.08.17.
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class StatSheet {
  private float slices;   // Number of slices used to compute
  private float silences; // Number of slices that are silent
  private float sum;      // Sum of score for all non-silent slices

  private float average; // sum/(slices-silences)
  private float min;     // Minimum value
  private float max;     // Maximum value
  private float sd;      // Standard deviation


  public StatSheet() {
    min = Float.MAX_VALUE;
    max = Float.MIN_VALUE;
    average = -1;
    sd = -1;
    sum = 0;
    slices = 0;
    silences = 0;
  }

  public void setAverage(float Average) {this.average = Average;}
  public float getAverage() {return average;}

  public void setMin(float min) {this.min = min;}
  public float getMin() {return min;}

  public void setMax(float max) {this.max = max;}
  public float getMax() {return max;}

  //Range can be returned on demand, no need to save it.
  public float getRange() {return max-min;}

  public void setSd(float sd) {this.sd = sd;}
  public float getSd() {return sd;}

  public void setSum(float sum) {this.sum = sum;}
  public void addSum(float addOn){this.sum += addOn;}
  public float getSum() {return sum;}

  public JSONObject toJSON() throws JSONException{
    JSONObject json = new JSONObject();
    json.put("average", average);
    json.put("min", min);
    json.put("max", max);
    json.put("range", max-min);
    json.put("sd", sd);
    json.put("sum", sum);
    return json;
  }

}
