const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  devServer:{
    port: 8889
  },
  transpileDependencies: true,
  chainWebpack: config => {
    config.plugin('define').tap(definitions => {
        Object.assign(definitions[0]['process.env'], {
          NODE_HOST: '"http://playground-b2c-client.authing.co/api"',
        });
        return definitions;
    });
  },
})


