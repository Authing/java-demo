let proxyObj = {};
const CompressionPlugin = require("compression-webpack-plugin");
// proxyObj['/ws'] = {
//     ws: true,
//     target: "ws://120.26.72.139:8082"
// };
// proxyObj['/'] = {
//     ws: false,
//     target: 'http://localhost:8081',
//     changeOrigin: true,
//     pathRewrite: {
//         '^/': ''
//     }
// }
proxyObj['/api'] = {
    ws: false,
    target: 'http://localhost:8081',
    changeOrigin: true,
    pathRewrite: {
        '^/api': ''
    }
}
module.exports = {
    devServer: {
        host: 'localhost',
        port: 8080,
        proxy: proxyObj
    },
    configureWebpack: config => {
        if (process.env.NODE_ENV === 'production') {
            return {
                plugins: [
                    new CompressionPlugin({
                        test: /\.js$|\.html$|\.css/,
                        threshold: 1024,
                        deleteOriginalAssets: false
                    })
                ]
            }
        }
    }
}