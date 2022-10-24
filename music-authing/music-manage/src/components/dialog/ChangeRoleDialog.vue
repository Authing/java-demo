<template>
    <div>
      <!-- 修改角色提示框 -->
      <el-dialog title="修改角色" @close="cancelRowRole" v-model="roleDialogVisible" width="300px" center>
            <el-select v-model="roleIds" multiple placeholder="请选择角色">
                <el-option
                    v-for="item in roleOptions"
                    :key="item.roleId"
                    :label="item.roleName"
                    :value="item.roleId"
                ></el-option>
            </el-select>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="cancelRowRole">取 消</el-button>
                <el-button type="primary" @click="confirmRole">确 定</el-button>
            </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script lang="ts">
  import { defineProps,defineComponent, getCurrentInstance, toRefs, watch, ref } from "vue";

  export default defineComponent({
    props: {
      roleVisible: Boolean,
      passRoleIds: Array
    },
    emits: ["cancelRowRole", "confirmRole"],
    setup(props) {
      const { proxy } = getCurrentInstance();
      const { passRoleIds } = toRefs(props);

      const roleIds = ref(passRoleIds.value);

      const roleOptions = [{
                roleId: '1',
                roleName: '普通用户'
            },{
                roleId: '2',
                roleName: 'vip'
            },{
                roleId: '3',
                roleName: '普通管理员'
            }];
  
      const { roleVisible } = toRefs(props);
      const roleDialogVisible = ref(roleVisible.value);
  
      watch(roleVisible, (value) => {
        roleDialogVisible.value = value;
      });    

      watch(
        () => passRoleIds,
        () => {
            roleIds.value = passRoleIds.value;
        }
      );
  
      function cancelRowRole() {
        proxy.$emit("cancelRowRole", false);
      }
      function confirmRole() {
        proxy.$emit("confirmRole", null);
      }
      return {
        roleDialogVisible,
        roleOptions,
        cancelRowRole,
        confirmRole,
      };
    },
  });
  </script>
  