cordova.define("cordova-plugin-webresource.webresource", function(require, exports, module) {
  var exec = require('cordova/exec');

  var WebResource = {
    getVersion: function(success, error) {
      exec(success, error, "WebResource", "getVersion", []);
    },
    getResource: function(files, success, error) {
      if(typeof files !== "object") {
        files = [files];
      }
      var files2 = [];
      var file = '';
      var pos = -1;
      for(var i=0; i<files.length; i++) {
        file = (""+files[i]).replace(/http:\/\//, '');
        pos = file.indexOf('/');
        if(pos === 0) {
          file = file.substr(pos+1);
        }
        files2.push(file);
      }
      exec(success, error, "WebResource", "getResource", files2);
    },
    checkAndUpdate: function(version, zipUrl, success, error) {
      exec(success, error, "WebResource", "checkAndUpdate", [version, zipUrl]);
    }
  };

  module.exports = WebResource;
})