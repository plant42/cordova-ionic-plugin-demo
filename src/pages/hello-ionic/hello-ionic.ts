import {Component, ChangeDetectorRef, ChangeDetectionStrategy, Input} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';

declare var window;
declare var $;

import Highcharts from 'highcharts/highstock';
window.Highcharts = Highcharts;


@Component({
  selector: 'page-hello-ionic',
  templateUrl: 'hello-ionic.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})


export class HelloIonicPage {
  @Input() addItemStream: Observable<any>;
  public dateVar: number = 0;
  public item: any;

  constructor(private ref: ChangeDetectorRef) {

  }

  callbackStream = (e) => {
    console.log(e);
    this.item = e;
    this.ref.markForCheck();
    this.ref.detectChanges();
    navigator.vibrate(30);
    if (this.item.pace.now < 30) {
      navigator.vibrate(150);
    }
  };

  startRecording() {
    window.AudioStatLogger.startStream(this.callbackStream);
  }

  stopRecording() {
    console.log(window);
    window.AudioStatLogger.stopStream();
    navigator.vibrate([300, 50, 150]);
  }

  clearLog() {
    this.drawGauge();
    console.log("Clearing arraylists");
    window.AudioStatLogger.clearLog();
    navigator.vibrate(400);
  }



  drawGauge(){
    var gaugeOptions = {

      chart: {
        type: 'solidgauge'
      },

      title: null,

      pane: {
        center: ['50%', '85%'],
        size: '140%',
        startAngle: -90,
        endAngle: 90,
        background: {
          backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
          innerRadius: '60%',
          outerRadius: '100%',
          shape: 'arc'
        }
      },

      tooltip: {
        enabled: false
      },

      // the value axis
      yAxis: {
        stops: [
          [0.1, '#55BF3B'], // green
          [0.5, '#DDDF0D'], // yellow
          [0.9, '#DF5353'] // red
        ],
        lineWidth: 0,
        minorTickInterval: null,
        tickAmount: 2,
        title: {
          y: -70
        },
        labels: {
          y: 16
        }
      },

      plotOptions: {
        solidgauge: {
          dataLabels: {
            y: 5,
            borderWidth: 0,
            useHTML: true
          }
        }
      }
    };

    // The speed gauge
    var chartSpeed = Highcharts.chart('container-speed', Highcharts.merge(gaugeOptions, {
      yAxis: {
        min: 0,
        max: 200,
        title: {
          text: 'Speed'
        }
      },

      credits: {
        enabled: false
      },

      series: [{
        name: 'Speed',
        data: [80],
        dataLabels: {
          format: '<div style="text-align:center"><span style="font-size:25px;color:' +
          ((Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black') + '">{y}</span><br/>' +
          '<span style="font-size:12px;color:silver">km/h</span></div>'
        },
        tooltip: {
          valueSuffix: ' km/h'
        }
      }]

    }));

    // The RPM gauge
    var chartRpm = Highcharts.chart('container-rpm', Highcharts.merge(gaugeOptions, {
      yAxis: {
        min: 0,
        max: 5,
        title: {
          text: 'RPM'
        }
      },

      series: [{
        name: 'RPM',
        data: [1],
        dataLabels: {
          format: '<div style="text-align:center"><span style="font-size:25px;color:' +
          ((Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black') + '">{y:.1f}</span><br/>' +
          '<span style="font-size:12px;color:silver">* 1000 / min</span></div>'
        },
        tooltip: {
          valueSuffix: ' revolutions/min'
        }
      }]

    }));
  }



}

