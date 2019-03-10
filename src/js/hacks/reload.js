module.exports = function reload (moduleName) {
  delete require.cache[require.resolve(moduleName)];
  return require(moduleName);
};
