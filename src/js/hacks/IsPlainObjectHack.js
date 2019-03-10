const checks = require("@thi.ng/checks");

checks.isPlainObject = function isPlainObject (x) {
  return Object.prototype.toString.call(x) === "[object Object]"
    && x
    && x.constructor
    && x.constructor.toString() === "function Object() { [native code] }";
}
