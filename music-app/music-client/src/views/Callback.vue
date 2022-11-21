<template>
    <div>
    </div>
</template>
<script lang="ts" setup>
    import { onMounted,getCurrentInstance } from 'vue'
    import { useRouter } from 'vue-router'
    import { useGuard } from '@authing/guard-vue3'
    import type { JwtTokenStatus, User } from '@authing/guard-vue3'

    import { HttpManager } from "@/api";
    import { NavName, RouterName } from "@/enums";
    import mixin from "@/mixins/mixin";

    const { routerManager, changeIndex } = mixin();

    const router = useRouter()

    const guard = useGuard()

    let internalInstance = getCurrentInstance();
    let cookies = internalInstance.appContext.config.globalProperties.cookies;
    const { proxy } = getCurrentInstance();

    const handleAuthingLoginCallback = async () => {
    try {
        // 1. 触发 guard.handleRedirectCallback() 方法完成登录认证
        // 用户认证成功之后，我们会将用户的身份凭证存到浏览器的本地缓存中
        await guard.handleRedirectCallback()

        // 2. 处理完 handleRedirectCallback 之后，你需要先检查用户登录态是否正常
        const loginStatus: JwtTokenStatus | undefined = await guard.checkLoginStatus()
        // console.log("loginStatus:",loginStatus)

        if (!loginStatus) {
            guard.startWithRedirect({
                scope: 'openid profile'
            })
            console.error("用户信息未获取到")
            return
        }else{
            cookies.set("GuardLogin", true);
        }

        // 3. 获取到登录用户的用户信息
        const userInfo: User | null = await guard.trackSession()
        // console.log(userInfo)

        const params = new URLSearchParams();
        params.append("token", userInfo.token);
        const result = (await HttpManager.getAccessTokenByToken(params)) as ResponseBody
        
        if(userInfo){
            proxy.$store.commit("setUserId", userInfo.id);
            proxy.$store.commit("setUsername", userInfo.username);
            proxy.$store.commit("setToken", true);

            cookies.set("userAccessToken",result.data);
        }


        // 你也可以重定向到你的任意业务页面，比如重定向到用户的个人中心
        // 如果你希望实现登录后跳转到同一页面的效果，可以通过在调用 startWithRedirect 时传入的自定义 state 实现
        // 之后你在这些页面可以通过 trackSession 方法获取用户登录态和用户信息

        // 示例一：跳转到固定页面
        changeIndex(NavName.Home);
        routerManager(RouterName.Home, { path: RouterName.Home });

        // 示例二：获取自定义 state，进行特定操作
        // const search = window.location.search
        // 从 URL search 中解析 state
    } catch (e) {
        // 登录失败，推荐再次跳转到登录页面
        guard.startWithRedirect({
            scope: 'openid profile'
        })
    }
    }

    onMounted(() => {
        handleAuthingLoginCallback()
    })

  </script>
  