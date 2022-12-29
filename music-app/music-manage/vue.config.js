const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  devServer:{
    port: 8890
  },
  transpileDependencies: true,
  chainWebpack: config => {
    config.plugin('define').tap(definitions => {
        Object.assign(definitions[0]['process.env'], {
          NODE_HOST: '"http://123.57.234.59:8890/api"',
        });
        return definitions;
    });
  }
})
