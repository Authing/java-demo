<template>
  <div class="sidebar">
    <el-menu
      class="sidebar-el-menu"
      background-color="#ffffff"
      active-text-color="#30a4fc"
      default-active="2"
      router
      :collapse="collapse"
    >
      <el-menu-item index="info">
        <el-icon><pie-chart /></el-icon>
        <span>系统首页</span>
      </el-menu-item>
      <el-menu-item index="consumer">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      <el-menu-item index="Role" v-if="menuList[8].meta.visiable">
        <el-icon><User /></el-icon>
        <span>角色管理</span>
      </el-menu-item>
      <el-menu-item index="singer">
        <el-icon><mic /></el-icon>
        <span>歌手管理</span>
      </el-menu-item>
      <el-menu-item index="songList">
        <el-icon><Document /></el-icon>
        <span>歌单管理</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script lang="ts" setup>
import { ref,defineComponent } from "vue";
import { PieChart, Mic, Document, User } from "@element-plus/icons-vue";
import emitter from "@/utils/emitter";
import routes from "@/router/index";
import { HttpManager } from "@/api/index";

// export default defineComponent({
//   setup(){
    const menuList = routes.options.routes[0].children;
    getUserRoles();
    async function getUserRoles() {
      const result = (await HttpManager.selectRoles()) as ResponseBody;
      if(result.success){
        let userRoles = result.data;
        console.log("userRoles:",userRoles);
        menuList.forEach(element => {
          // 1- for 循环一级菜单
          // 2-找出角色 所在的 角色数组(判断某个值在不在 数组中)
          // 3- 然后 所在的数组 visiable 改为true ，其他的改为false
          if (element.meta.roles){
            // let roleArray = JSON.parse(userRoles);
            if(userRoles){
              userRoles.forEach(role => {
              if(Object.values(element.meta.roles).includes(role)){
                element.meta.visiable = true;
              }else{
                element.meta.visiable = false;
              }
            })
            }
          }
        });
      }
    }
  
    const collapse = ref(false);
    emitter.on("collapse", (msg) => {
      collapse.value = msg as boolean;
    });

    // return{
    //   collapse,
    //   menuList,
    //   getUserRoles,
    // };
//   }
// })
</script>

<style scoped>
.sidebar {
  display: block;
  position: absolute;
  left: 0;
  top: 60px;
  bottom: 0;
  overflow-y: scroll;
}

.sidebar::-webkit-scrollbar {
  width: 0;
}

.sidebar > ul {
  height: 100%;
}

.sidebar-el-menu:not(.el-menu--collapse) {
  width: 150px;
}
</style>
