<template>
  <div class="container">
    <div class="handle-box">
      <el-button type="danger" @click="deleteAll">批量删除</el-button>
      <el-input v-model="searchWord" placeholder="筛选帖子标题或内容" clearable style="width: 300px"></el-input>
    </div>

    <el-table 
      height="550px" 
      border 
      size="small" 
      :data="data" 
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center"></el-table-column>
      <el-table-column label="ID" prop="postId" width="70" align="center"></el-table-column>
      <el-table-column label="标题" prop="title" width="200" align="center"></el-table-column>
      <el-table-column label="内容" min-width="300">
        <template v-slot="scope">
          <div class="content-preview">{{ scope.row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column label="标签" width="180" align="center">
        <template v-slot="scope">
          <el-tag 
            v-for="(tag, index) in scope.row.tags" 
            :key="index" 
            size="small" 
            style="margin: 2px"
          >
            {{ tag }}
          </el-tag>
          <span v-if="!scope.row.tags || scope.row.tags.length === 0">-</span>
        </template>
      </el-table-column>
      <el-table-column label="发帖人" prop="username" width="120" align="center">
        <template v-slot="scope">
          <div>{{ scope.row.username || '用户' + scope.row.userId }}</div>
        </template>
      </el-table-column>
      <el-table-column label="浏览" prop="viewCount" width="70" align="center"></el-table-column>
      <el-table-column label="附件" prop="attachmentCount" width="70" align="center">
        <template v-slot="scope">
          <div>{{ scope.row.attachmentCount || 0 }}</div>
        </template>
      </el-table-column>
      <el-table-column label="评论" prop="commentCount" width="70" align="center">
        <template v-slot="scope">
          <div>{{ scope.row.commentCount || 0 }}</div>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160" align="center">
        <template v-slot="scope">
          <div>{{ formatDate(scope.row.createdAt) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template v-slot="scope">
          <el-button type="danger" size="small" @click="deleteRow(scope.row.postId)">删除</el-button>
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
  <yin-del-dialog 
    :delVisible="delVisible" 
    @confirm="confirm" 
    @cancelRow="delVisible = $event"
  ></yin-del-dialog>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, watch, ref, computed } from "vue";
import { HttpManager } from "@/api";
import YinDelDialog from "@/components/dialog/YinDelDialog.vue";
import { ElMessage } from "element-plus";
import axios from "axios";

export default defineComponent({
  components: {
    YinDelDialog,
  },
  setup() {
    const { proxy } = getCurrentInstance();

    const tableData = ref([]); // 记录帖子，用于显示
    const tempDate = ref([]); // 记录帖子，用于搜索时能临时记录一份帖子列表
    const pageSize = ref(10); // 每页显示数量
    const currentPage = ref(1); // 当前页

    // 计算当前表格中的数据
    const data = computed(() => {
      return tableData.value.slice(
        (currentPage.value - 1) * pageSize.value,
        currentPage.value * pageSize.value
      );
    });

    const searchWord = ref(""); // 记录输入框输入的内容
    watch(searchWord, () => {
      if (searchWord.value === "") {
        tableData.value = tempDate.value;
      } else {
        tableData.value = tempDate.value.filter(
          (item) =>
            item.title?.includes(searchWord.value) || 
            item.content?.includes(searchWord.value)
        );
      }
      currentPage.value = 1;
    });

    const delVisible = ref(false); // 删除提示框可见性
    const multipleSelection = ref([]); // 记录选中的数据
    const idx = ref(-1); // 记录单个删除的下标

    // 获取所有帖子
    async function getData() {
      try {
        const result = await axios.get("http://localhost:8888/posts");
        console.log("获取到的帖子数据:", result.data);
        tableData.value = result.data;
        tempDate.value = result.data;
        currentPage.value = 1;
      } catch (error) {
        console.error("获取帖子列表失败:", error);
      }
    }

    // 删除
    function deleteRow(postId) {
      idx.value = postId;
      delVisible.value = true;
    }

    // 批量删除
    function deleteAll() {
      if (!multipleSelection.value || multipleSelection.value.length === 0) {
        return;
      }
      delVisible.value = true;
    }

    // 确认删除
    async function confirm() {
      try {
        if (idx.value !== -1) {
          // 单个删除
          await axios.delete(`http://localhost:8888/posts/${idx.value}`, {
            headers: {
              'X-Admin': 'true'
            }
          });
          ElMessage.success('删除成功');
          getData();
        } else {
          // 批量删除
          const deletePromises = multipleSelection.value.map((item) =>
            axios.delete(`http://localhost:8888/posts/${item.postId}`, {
              headers: {
                'X-Admin': 'true'
              }
            })
          );
          await Promise.all(deletePromises);
          ElMessage.success(`成功删除 ${multipleSelection.value.length} 条帖子`);
          getData();
        }
      } catch (error) {
        console.error("删除失败:", error);
        ElMessage.error('删除失败，请稍后重试');
      }
      delVisible.value = false;
      idx.value = -1;
    }

    // 分页
    function handleCurrentChange(val) {
      currentPage.value = val;
    }

    // 选中行
    function handleSelectionChange(val) {
      multipleSelection.value = val;
    }

    // 格式化日期 - 处理Java LocalDateTime的数组格式
    function formatDate(dateValue) {
      if (!dateValue) return '';
      
      try {
        // 如果是数组格式 [年, 月, 日, 时, 分, 秒]
        if (Array.isArray(dateValue)) {
          const [year, month, day, hour, minute, second] = dateValue;
          // 月份需要减1，因为JavaScript的月份是0-11
          const date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0);
          
          // 格式化为 YYYY-MM-DD HH:mm:ss
          const y = date.getFullYear();
          const m = String(date.getMonth() + 1).padStart(2, '0');
          const d = String(date.getDate()).padStart(2, '0');
          const h = String(date.getHours()).padStart(2, '0');
          const min = String(date.getMinutes()).padStart(2, '0');
          const s = String(date.getSeconds()).padStart(2, '0');
          
          return `${y}-${m}-${d} ${h}:${min}:${s}`;
        }
        
        // 如果是字符串格式
        const date = new Date(dateValue);
        if (isNaN(date.getTime())) return '';
        
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        const h = String(date.getHours()).padStart(2, '0');
        const min = String(date.getMinutes()).padStart(2, '0');
        const s = String(date.getSeconds()).padStart(2, '0');
        
        return `${y}-${m}-${d} ${h}:${min}:${s}`;
        
      } catch (e) {
        console.error("时间格式化失败:", e);
        return '';
      }
    }

    getData();

    return {
      tableData,
      data,
      pageSize,
      currentPage,
      searchWord,
      delVisible,
      formatDate,
      deleteRow,
      deleteAll,
      confirm,
      handleCurrentChange,
      handleSelectionChange,
      attachImageUrl: HttpManager.attachImageUrl,
    };
  },
});
</script>

<style scoped>
.handle-box {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.content-preview {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  font-size: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
