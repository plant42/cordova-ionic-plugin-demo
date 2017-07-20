
var exec = require('cordova/exec');

var PLUGIN_NAME = 'MyCordovaPlugin';

var MyCordovaPlugin = {
  echo: function(phrase, cb) {
	console.log("Called Echo");
    exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
  },
  getDate: function(cb) {
	console.log("Called Get Date");
    exec(cb, null, PLUGIN_NAME, 'getDate', []);
  }
};

module.exports = MyCordovaPlugin;
