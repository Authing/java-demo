<template>
  <div class="container">
    <div class="handle-box">
      <el-button type="danger" @click="deleteAll">批量删除</el-button>
      <el-button type="primary" @click="addRole">新增角色</el-button>
      <el-input v-model="searchWord" placeholder="筛选角色"></el-input>
    </div>

    <el-table height="550px" border size="small" :data="data" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" align="center"></el-table-column>
      <el-table-column label="code" prop="code" width="150" align="center"></el-table-column>
      <el-table-column label="角色描述" prop="description" align="center"></el-table-column>
      <el-table-column label="操作" width="300" align="center">
        <template v-slot="scope">
          <el-button type="danger" @click="deleteRow(scope.row.code)">删除</el-button>
          <el-button type="primary" @click="changeRole(scope.row)">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        class="pagination"
        background
        layout="total, prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="tableData.length"
        @current-change="handleCurrentChange"
    >
    </el-pagination>
  </div>

  <!-- 删除提示框 -->
  <yin-del-dialog :delVisible="delVisible" @confirm="confirm" @cancelRow="delVisible = $event"></yin-del-dialog>

  <!-- 修改角色提示框 -->
  <el-dialog title="修改" v-model="editVisible" @close="editVisible = false" width="300px" center>
    <el-form label-width="60px" :model="roleForm" :rules="rule">
      <el-form-item label="code" prop="code">
        <el-input v-model="roleForm.code" readonly></el-input>
      </el-form-item>
      <el-form-item label="newCode" prop="newCode">
        <el-input v-model="roleForm.newCode"></el-input>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="roleForm.description"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
              <span class="dialog-footer">
                  <el-button @click="editVisible = false">取 消</el-button>
                  <el-button type="primary" @click="saveEdit">确 定</el-button>
              </span>
    </template>
  </el-dialog>

  <!-- 添加角色提示框 -->
  <el-dialog title="添加" v-model="addVisible" @close="addVisible = false" width="300px" center>
    <el-form label-width="60px" :model="addRoleForm" :rules="addRule">
      <el-form-item label="code" prop="code">
        <el-input v-model="addRoleForm.code"></el-input>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="addRoleForm.description"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
              <span class="dialog-footer">
                  <el-button @click="addVisible = false">取 消</el-button>
                  <el-button type="primary" @click="addRoleSave">确 定</el-button>
              </span>
    </template>
  </el-dialog>

</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, watch, ref, reactive, computed } from "vue";
import mixin from "@/mixins/mixin";
import { HttpManager } from "@/api";
import YinDelDialog from "@/components/dialog/YinDelDialog.vue";
import { getBirth } from "@/utils";

export default defineComponent({
  components: {
    YinDelDialog,
  },
  setup() {

    /**
     * 添加角色
     */
    const addVisible = ref(false);
    const addRule = reactive({
      code: [{ required: true, trigger: "change" }],
    });
    const addRoleForm = reactive({
      code:'',
      description:'',
    });
    function addRole(){
      addVisible.value = true;
    }
    async function addRoleSave() {
      try{
        let params = new URLSearchParams();
        params.append("code", addRoleForm.code);
        params.append("description", addRoleForm.description);

        const result = (await HttpManager.addRole(params)) as ResponseBody;
        (proxy as any).$message({
          message: result.message,
          type: result.type,
        });

        if (result.success) getData();
        addVisible.value = false;
      }catch(error){
        console.error(error);
      }
    }

    /**
     * 修改角色
     */
    const editVisible = ref(false);
    const editRoleId = ref(-1);
    const rule = reactive({
      code: [{ required: true, trigger: "change" }],
    });
    const roleForm = reactive({
      code:'',
      newCode:'',
      description:'',
    });

    function changeRole(row){
      editRoleId.value = row.id;
      roleForm.code = row.code;
      roleForm.newCode = row.code;
      roleForm.description = row.description;
      editVisible.value = true;
    }

    async function saveEdit() {
      try {
        let params = new URLSearchParams();
        params.append("code",roleForm.code);
        params.append("newCode", roleForm.newCode);
        params.append("description",roleForm.description);

        const result = (await HttpManager.editRole(params)) as ResponseBody;
        (proxy as any).$message({
          message: result.message,
          type: result.type,
        });

        if (result.success) getData();
        editVisible.value = false;
      } catch (error) {
        console.error(error);
      }
    }

    const { proxy } = getCurrentInstance();
    const { changeSex, routerManager } = mixin();

    const tableData = ref([]); // 记录歌曲，用于显示
    const tempDate = ref([]); // 记录歌曲，用于搜索时能临时记录一份歌曲列表
    const pageSize = ref(10); // 页数
    const currentPage = ref(1); // 当前页

    // 计算当前表格中的数据
    const data = computed(() => {
      return tableData.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value);
    });

    const searchWord = ref(""); // 记录输入框输入的内容
    watch(searchWord, () => {
      if (searchWord.value === "") {
        tableData.value = tempDate.value;
      } else {
        tableData.value = [];
        for (let item of tempDate.value) {
          if (item.username.includes(searchWord.value)) {
            tableData.value.push(item);
          }
        }
      }
    });

    getData();

    // 获取用户信息
    async function getData() {
      tableData.value = [];
      tempDate.value = [];
      const result = (await HttpManager.getAllRole()) as ResponseBody;
      tableData.value = result.data;
      tempDate.value = result.data;
      currentPage.value = 1;
    }
    // 获取当前页
    function handleCurrentChange(val) {
      currentPage.value = val;
    }

    /**
     * 删除
     */
    const idx = ref(-1); // 记录当前要删除的行
    const multipleSelection = ref([]); // 记录当前要删除的列表
    const delVisible = ref(false); // 显示删除框

    async function confirm() {
      const result = (await HttpManager.deleteRole(idx.value)) as ResponseBody;
      (proxy as any).$message({
        message: result.message,
        type: result.type,
      });
      if (result) getData();
      delVisible.value = false;
    }

    function deleteRow(code) {
      idx.value = code;
      delVisible.value = true;
    }
    function handleSelectionChange(val) {
      multipleSelection.value = val;
    }
    function deleteAll() {
      for (let item of multipleSelection.value) {
        deleteRow(item.code);
        confirm();
      }
      multipleSelection.value = [];
    }

    return {
      searchWord,
      data,
      tableData,
      delVisible,
      pageSize,
      currentPage,
      editVisible,
      roleForm,
      rule,
      addRule,
      addVisible,
      addRoleForm,
      addRoleSave,
      addRole,
      saveEdit,
      deleteAll,
      handleSelectionChange,
      handleCurrentChange,
      changeSex,
      getBirth,
      deleteRow,
      changeRole,
      confirm,
      attachImageUrl: HttpManager.attachImageUrl,
    };
  },
});
</script>

<style scoped></style>
  