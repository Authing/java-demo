import { createApp } from "vue";
import ElementPlus from "element-plus";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import "element-plus/dist/index.css";
import "./assets/css/index.scss";
import "./assets/icons/index.js";
import VueCookies from 'vue-cookies'
import { Store } from "vuex";
import { createGuard } from "@authing/guard-vue3";
import "@authing/guard-vue3/dist/esm/guard.min.css";
declare module "@vue/runtime-core" {
  interface State {
    count: number;
  }

  interface ComponentCustomProperties {
    $store: Store<State>;
  }
}

const app = createApp(App);
app.config.globalProperties.cookies = VueCookies;
app.use(
  createGuard({
    //todo
    appId: "634521175dc75ced7033ccdb",
    // 如果你使用的是私有化部署的 Authing 服务，需要传入自定义 host，如:
    host: 'https://kuluqi3.authing.cn',

    // 默认情况下，会使用你在 Authing 控制台中配置的第一个回调地址为此次认证使用的回调地址。
    // 如果你配置了多个回调地址，也可以手动指定（此地址也需要加入到应用的「登录回调 URL」中）：
    redirectUri: "http://127.0.0.1:8080/callback",
    // mode: "normal",
  })
);
app.use(store).use(router).use(ElementPlus).mount("#app");


