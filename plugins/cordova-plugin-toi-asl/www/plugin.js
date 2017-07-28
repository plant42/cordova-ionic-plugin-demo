
var exec = require('cordova/exec');

var PLUGIN_NAME = 'AudioStatLogger';

var AudioStatLogger = {
  echo: function(phrase, cb) {
	console.log("Called Echo");
    exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
  },
  startStream: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'startStream', []);
  },
  stopStream: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'stopStream', []);
  }
};

module.exports = AudioStatLogger;
