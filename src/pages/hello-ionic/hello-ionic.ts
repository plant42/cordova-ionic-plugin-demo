import { Component } from '@angular/core';

declare var cordova;
declare var window;


@Component({
  selector: 'page-hello-ionic',
  templateUrl: 'hello-ionic.html'
})

export class HelloIonicPage {
	public dateVar:number = 0;

  constructor() {

	
  }

callback = (e) => { 
		console.log(e);
		console.log("In Callback");
		this.dateVar = e;
	};

buttonClick() {
	console.log(window);
	console.log(cordova);
	window.MyCordovaPlugin.getDate(this.callback);

	console.log("got here 333");
 }

  
	
	
}
