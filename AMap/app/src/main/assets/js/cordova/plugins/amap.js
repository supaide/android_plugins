cordova.define("cordova-plugin-map.amap", function(require, exports, module) {
  var exec = require('cordova/exec');

  var AMap = {
    getLocation: function(success, error) {
      exec(success, error, "AMap", "getLocation", []);
    },
    poiSearch: function(success, error, keyword, city, page, pageSize) {
        exec(success, error, "AMap", "poiSearch", [keyword, city, page, pageSize]);
    }
  };

  module.exports = AMap;
})