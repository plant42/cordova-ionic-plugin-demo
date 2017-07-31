import {Component, ChangeDetectorRef, ChangeDetectionStrategy, Input} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';

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
  public dateVar: number = 0;
  public item: any;


  constructor(private ref: ChangeDetectorRef) {

  }

  callbackStream = (e) => {
    console.log(e);
    this.item = e;
    this.ref.markForCheck();
    this.ref.detectChanges();
    //navigator.vibrate(30);
    if (this.item.pace.now < 30) {
      //navigator.vibrate(150);
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
    console.log("Clearing arraylists");
    window.AudioStatLogger.clearLog();
    navigator.vibrate(400);
  }


}

