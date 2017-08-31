import {Component, ChangeDetectorRef, ChangeDetectionStrategy, Input, ViewChild} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import {Chart} from 'chart.js';
import {Gauge} from 'gauge.js';

declare var window;

import Highcharts from 'highcharts/highstock';
window.Highcharts = Highcharts;


@Component({
  selector: 'page-hello-ionic',
  templateUrl: 'hello-ionic.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})


export class HelloIonicPage {
  @Input() addItemStream: Observable<any>;
  @ViewChild('gaugeCanvas') gaugeCanvas;

  public counter: number = 0;
  public item: any;
  public gaugeChart: any;


  constructor(private ref: ChangeDetectorRef) {

  }

  ionViewDidLoad() {
    Chart.pluginService.register({
      afterUpdate: function (chart) {
        if (chart.config.options.elements.center) {
          var helpers = Chart.helpers;
          var centerConfig = chart.config.options.elements.center;
          var globalConfig = Chart.defaults.global;
          var ctx = chart.chart.ctx;

          var fontStyle = helpers.getValueOrDefault(centerConfig.fontStyle, globalConfig.defaultFontStyle);
          var fontFamily = helpers.getValueOrDefault(centerConfig.fontFamily, globalConfig.defaultFontFamily);

          if (centerConfig.fontSize)
            var fontSize = centerConfig.fontSize;
          // figure out the best font size, if one is not specified
          else {
            ctx.save();
            var fontSize = helpers.getValueOrDefault(centerConfig.minFontSize, 1);
            var maxFontSize = helpers.getValueOrDefault(centerConfig.maxFontSize, 256);
            var maxText = helpers.getValueOrDefault(centerConfig.maxText, centerConfig.text);

            do {
              ctx.font = helpers.fontString(fontSize, fontStyle, fontFamily);
              var textWidth = ctx.measureText(maxText).width;

              // check if it fits, is within configured limits and that we are not simply toggling back and forth
              if (textWidth < chart.innerRadius * 2 && fontSize < maxFontSize)
                fontSize += 1;
              else {
                // reverse last step
                fontSize -= 1;
                break;
              }
            } while (true)
            ctx.restore();
          }

          // save properties
          chart.center = {
            font: helpers.fontString(fontSize, fontStyle, fontFamily),
            fillStyle: helpers.getValueOrDefault(centerConfig.fontColor, globalConfig.defaultFontColor)
          };
        }
      },
      afterDraw: function (chart) {
        if (chart.center) {
          var centerConfig = chart.config.options.elements.center;
          var ctx = chart.chart.ctx;

          ctx.save();
          ctx.font = chart.center.font;
          ctx.fillStyle = chart.center.fillStyle;
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          var centerX = (chart.chartArea.left + chart.chartArea.right) / 2;
          var centerY = (chart.chartArea.top + chart.chartArea.bottom) / 2;
          ctx.fillText(centerConfig.text, centerX, centerY);
          ctx.restore();
        }
      },
    });


    this.gaugeChart = new Chart(this.gaugeCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ["PitchAverage"],
          datasets: [
            {
              label: ['', '', ''],
              data: [1, 1, 1],
              backgroundColor: [
                '#17bebb',
                '#ff2e63',
                '#939999'
              ],
            }/*,
            {
              data: [2, 1, 2],
              backgroundColor: [
                '#ff0000',
                '#00ff00',
                '#0000ff'
              ],
            },
            */
          ]
      },
      options: {
        legend: {
          display: false,
        },
        rotation: 0.75 * Math.PI,
        circumference: 1.5 * Math.PI,
        cutoutPercentage: 25,
        elements: { //Experiemntal
          center: {
            text: 'Pitch',
            color: '#FF6384', // Default is #000000
            fontStyle: 'Arial', // Default is Arial
            sidePadding: 2 // Defualt is 20 (as a percentage)
          }
        }
      }
    });
  }

  limitNum(num:number, limit:number):number{
    if (num < 0) return 0;
    if (num > limit) return limit;
  }

  callbackStream = (e) => {
    this.counter++;
    console.log(e);
    this.item = e;
    this.ref.markForCheck();
    this.ref.detectChanges();
    //navigator.vibrate(30);
    if (this.item.pace.last < 30) {
      //navigator.vibrate(150);
    }

    var pitchAvgNow: number = e.pitchAverage.mean; //Mean of last 5 recordings = now
    if (pitchAvgNow != -1) {
      if (pitchAvgNow > 500) pitchAvgNow = 500;
      this.gaugeChart.data.datasets[0].data[0] = pitchAvgNow;
      this.gaugeChart.data.datasets[0].data[1] = 10;
      this.gaugeChart.data.datasets[0].data[2] = 500 - pitchAvgNow;
    }

    /*
    var pitchMin: number = e.pitchAverage.min;
    var pitchMax: number = e.pitchAverage.max;
    if (pitchMin != -1 && pitchMax != -1){
      pitchMin = this.limitNum(pitchMin, 500);
      pitchMax = this.limitNum(pitchMax, 500);

      //TODO: Fix min max range viewer
      this.gaugeChart.data.datasets[1].data[0] = pitchMin;
      this.gaugeChart.data.datasets[1].data[1] = pitchMax-pitchMin;
      this.gaugeChart.data.datasets[1].data[2] = 500-pitchMax;
    }
    */

    this.gaugeChart.update();
  };


  startLog() {
    window.AudioStatLogger.startLog(this.callbackStream);
    navigator.vibrate([150, 50, 300]);
  }

  stopLog() {
    console.log(window);
    window.AudioStatLogger.stopLog();
    navigator.vibrate([300, 50, 150]);
  }

  clearLog() {
    console.log("Clearing arraylists");
    window.AudioStatLogger.clearLog();
    navigator.vibrate(400);
  }


}

