<template>
    <!-- <yin-login-logo></yin-login-logo> -->
    <div class="sign">
      <!-- <div class="sign-head">
        <span>帐号登录</span>
      </div> -->
      <el-form>
        <el-form-item>
          <el-button class="login-btn" type="primary" @click="changeContentCSS">change css</el-button>
        </el-form-item>
        <el-form-item>
          <div id="authing-guard-container"></div>
        </el-form-item>
      </el-form>
    </div>
</template>
  
<script lang="ts" setup>
    import { getCurrentInstance,onMounted } from "vue";
    import { useGuard } from "@authing/guard-vue3";
    import type { User } from "@authing/guard-vue3";
    import { useRouter } from 'vue-router';
    import { HttpManager } from "@/api";
    import { NavName, RouterName } from "@/enums";
    import mixin from "@/mixins/mixin";

    const { routerManager, changeIndex } = mixin();

    const guard = useGuard();
    const { proxy } = getCurrentInstance();
    const router = useRouter()

    let internalInstance = getCurrentInstance();
    let cookies = internalInstance.appContext.config.globalProperties.cookies;

    guard.on('login', (userInfo: User) => {
        console.log('userInfo in login: ', userInfo)
    })

    async function AuthingLogin() {
        // 使用 start 方法挂载 Guard 组件到你指定的 DOM 节点，登录成功后返回 userInfo
        guard.start("#authing-guard-container")

        const userInfo: User | null = await guard.trackSession()
        proxy.$store.commit("setUserId", userInfo.id);
        proxy.$store.commit("setUsername", userInfo.username);
        proxy.$store.commit("setToken", true);
        cookies.set("GuardLogin", true);

        const params = new URLSearchParams();
        params.append("token", userInfo.token);
        const result = (await HttpManager.getAccessTokenByToken(params)) as ResponseBody
        cookies.set("userAccessToken",result.data);

        changeIndex(NavName.Home);
        routerManager(RouterName.Home, { path: RouterName.Home });
    }
    onMounted(() => {
      AuthingLogin();
    });

    const changeContentCSS = () =>
      guard.changeContentCSS(`
        #authing-guard-container {
          display: flex;
          align-items: center;
          justify-content: center;
          margin-left: 500px;
        }
      `);
</script>