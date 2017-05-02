cordova.define("cordova-plugin-pay.pay", function(require, exports, module) {
  var exec = require('cordova/exec');

  var Pay = {
    pay: function(action, params, success, error) {
      exec(success, error, "Pay", action, [params]);
    }
  };

  module.exports = Pay;
})