var exec = require('cordova/exec');

exports.getImage = function(arg0, success, error) {
    exec(success, error, "ImagePicker", "getImage", [arg0]);
};
