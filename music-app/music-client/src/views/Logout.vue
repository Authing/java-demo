<template>
    <div>
    </div>
</template>
<script lang="ts" setup>
    //此页面仅用作登出回调后跳转
    import { useRouter } from 'vue-router'
    const router = useRouter()
    router.replace({
        name: 'home'
    })
</script>