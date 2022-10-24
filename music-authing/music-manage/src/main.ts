import { createApp } from "vue";
import ElementPlus from "element-plus";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import "element-plus/dist/index.css";
import "./assets/css/main.css";
import "./assets/icons/iconfont.js";

import { Store } from "vuex";
import VueCookies from 'vue-cookies'

declare module "@vue/runtime-core" {
  interface State {
    count: number;
  }

  interface ComponentCustomProperties {
    $store: Store<State>;
  }
}

const app = createApp(App);
app.config.globalProperties.$cookies = VueCookies;
app.use(store).use(router).use(ElementPlus).mount("#app");
