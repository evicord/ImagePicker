var exec = require('cordova/exec');

exports.getPictures = function(arg0, success, error) {
    exec(success, error, "ImagePicker", "getPictures", [arg0]);
};

exports.toBinary = function(arg0, success, error) {
    exec(success, error, "ImagePicker", "toBinary", [arg0]);
};