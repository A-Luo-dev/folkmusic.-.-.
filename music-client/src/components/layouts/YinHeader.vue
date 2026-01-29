<template>
  <div class="yin-header">
    <!--图标-->
    <div class="header-logo" @click="goPage()">
      <img src="@/assets/images/木吉他.svg" alt="logo" class="logo-icon" />
      <span>{{ musicName }}</span>
    </div>
    <yin-header-nav class="yin-header-nav" :styleList="headerNavList" :activeName="activeNavName" @click="goPage"></yin-header-nav>
    <!--搜索框-->
    <div class="header-search">
      <el-autocomplete
        v-model="keywords"
        :fetch-suggestions="querySearchAsync"
        placeholder="搜索歌曲或歌手"
        :prefix-icon="Search"
        @keyup.enter="goSearch()"
        @focus="showSearchHistory = true"
        @blur="hideSearchHistory"
        @select="handleSelect"
        clearable
      >
        <template #default="{ item }">
          <div class="search-item" @click="selectKeyword(item.value)">
            <i class="el-icon-search"></i>
            <span>{{ item.value }}</span>
            <i v-if="token" class="el-icon-close delete-btn" @click.stop="deleteSearchHistory(item.value)"></i>
          </div>
        </template>
      </el-autocomplete>
      
      <!-- 搜索记录面板 -->
      <div v-if="showSearchHistory && searchHistory.length > 0 && token" class="search-history-panel">
        <div class="search-history-header">
          <span>搜索记录</span>
          <el-button type="text" size="small" @click="clearAllSearchHistory">清空</el-button>
        </div>
        <div class="search-history-list">
          <div 
            v-for="(keyword, index) in searchHistory" 
            :key="index" 
            class="search-history-item"
            @click="selectKeyword(keyword)"
          >
            <i class="el-icon-time"></i>
            <span>{{ keyword }}</span>
            <i class="el-icon-close delete-btn" @click.stop="deleteSearchHistory(keyword)"></i>
          </div>
        </div>
      </div>
    </div>
    <!--设置-->
    <yin-header-nav v-if="!token" :styleList="signList" :activeName="activeNavName" @click="goPage"></yin-header-nav>
    <el-dropdown class="user-wrap" v-if="token" trigger="click">
      <el-image class="user" fit="contain" :src="attachImageUrl(userPic)" />
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-for="(item, index) in menuList" :key="index" @click.stop="goMenuList(item.path)">{{ item.name }}</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, getCurrentInstance, computed, reactive, onMounted, watch } from "vue";
import { Search } from "@element-plus/icons-vue";
import { useStore } from "vuex";
import YinHeaderNav from "./YinHeaderNav.vue";
import mixin from "@/mixins/mixin";
import { HEADERNAVLIST, SIGNLIST, MENULIST, MUSICNAME, RouterName, NavName } from "@/enums";
import { HttpManager } from "@/api";

export default defineComponent({
  components: {
    YinHeaderNav,
  },
  setup() {
    const { proxy } = getCurrentInstance();
    const store = useStore();
    const { changeIndex, routerManager } = mixin();

    const musicName = ref(MUSICNAME);
    const headerNavList = ref(HEADERNAVLIST); // 左侧导航栏
    const signList = ref(SIGNLIST); // 右侧导航栏
    const menuList = ref(MENULIST); // 用户下拉菜单项
    const keywords = ref("");
    const searchHistory = ref([]);
    const showSearchHistory = ref(false);
    const activeNavName = computed(() => store.getters.activeNavName);
    const userPic = computed(() => store.getters.userPic);
    const token = computed(() => store.getters.token);

    function goPage(path, name) {
      if (!path && !name) {
        changeIndex(NavName.Home);
        routerManager(RouterName.Home, { path: RouterName.Home });
      } else {
        changeIndex(name);
        routerManager(path, { path });
      }
    }

    function goMenuList(path) {
      if (path == RouterName.SignOut) {
        proxy.$store.commit("setToken", false);
        changeIndex(NavName.Home);
        routerManager(RouterName.Home, { path: RouterName.Home });
      } else {
        routerManager(path, { path });
      }
    }
    function goSearch() {
      if (keywords.value !== "") {
        // 保存搜索记录
        saveSearchHistory(keywords.value);
        proxy.$store.commit("setSearchWord", keywords.value);
        routerManager(RouterName.Search, { path: RouterName.Search, query: { keywords: keywords.value } });
        showSearchHistory.value = false;
      } else {
        (proxy as any).$message({
          message: "搜索内容不能为空",
          type: "error",
        });
      }
    }
    
    // 获取搜索记录
    async function getSearchHistory() {
      if (!token.value) {
        searchHistory.value = [];
        return;
      }
      
      try {
        const response = await HttpManager.getSearchHistory() as any;
        if (response.success) {
          searchHistory.value = response.data || [];
        }
      } catch (error) {
        console.error("获取搜索记录失败:", error);
      }
    }
    
    // 保存搜索记录
    async function saveSearchHistory(keyword) {
      if (!token.value || !keyword || keyword.trim() === "") {
        return;
      }
      
      try {
        await HttpManager.addSearchHistory(keyword);
        // 重新获取搜索记录
        await getSearchHistory();
      } catch (error) {
        console.error("保存搜索记录失败:", error);
      }
    }
    
    // 清空搜索记录
    async function clearAllSearchHistory() {
      if (!token.value) {
        return;
      }
      
      try {
        await HttpManager.clearSearchHistory();
        searchHistory.value = [];
        (proxy as any).$message({
          message: "搜索记录已清空",
          type: "success",
        });
      } catch (error) {
        console.error("清空搜索记录失败:", error);
        (proxy as any).$message({
          message: "清空搜索记录失败",
          type: "error",
        });
      }
    }
    
    // 删除单条搜索记录
    async function deleteSearchHistory(keyword) {
      if (!token.value) {
        return;
      }
      
      try {
        await HttpManager.removeSearchHistory(keyword);
        // 重新获取搜索记录
        await getSearchHistory();
      } catch (error) {
        console.error("删除搜索记录失败:", error);
      }
    }
    
    // 选择关键词
    function selectKeyword(keyword) {
      keywords.value = keyword;
      goSearch();
    }
    
    // 自动完成查询
    async function querySearchAsync(queryString, cb) {
      if (!token.value) {
        cb([]);
        return;
      }
      
      if (!queryString) {
        // 如果没有输入，显示搜索记录
        const suggestions = searchHistory.value.map(item => ({ value: item }));
        cb(suggestions);
        return;
      }
      
      // 过滤搜索记录
      const suggestions = searchHistory.value
        .filter(item => item.toLowerCase().includes(queryString.toLowerCase()))
        .map(item => ({ value: item }));
      
      cb(suggestions);
    }
    
    // 处理选择
    function handleSelect(item) {
      selectKeyword(item.value);
    }
    
    // 隐藏搜索记录面板
    function hideSearchHistory() {
      setTimeout(() => {
        showSearchHistory.value = false;
      }, 200);
    }
    
    // 监听token变化，当用户登录/退出时更新搜索记录
    watch(token, (newToken) => {
      if (newToken) {
        getSearchHistory();
      } else {
        searchHistory.value = [];
      }
    });
    
    // 组件挂载时获取搜索记录
    onMounted(() => {
      if (token.value) {
        getSearchHistory();
      }
    });

    return {
      musicName,
      headerNavList,
      signList,
      menuList,
      keywords,
      searchHistory,
      showSearchHistory,
      activeNavName,
      userPic,
      token,
      Search,
      goPage,
      goMenuList,
      goSearch,
      getSearchHistory,
      clearAllSearchHistory,
      deleteSearchHistory,
      selectKeyword,
      querySearchAsync,
      handleSelect,
      hideSearchHistory,
      attachImageUrl: HttpManager.attachImageUrl,
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

@media screen and (min-width: $sm) {
  .header-logo {
    margin: 0 1rem;
  }
}

@media screen and (max-width: $sm) {
  .header-logo {
    margin: 0 1rem;
    span {
      display: none;
    }
  }
  .header-search {
    display: none;
  }
}

.yin-header {
  position: fixed;
  width: 100%;
  height: $header-height;
  line-height: $header-height;
  padding: $header-padding;
  margin: $header-margin;
  background-color: $theme-header-color;
  box-shadow: $box-shadow;
  box-sizing: border-box;
  z-index: 100;
  display: flex;
  white-space: nowrap;
  flex-wrap: nowrap;
}

/* LOGO */
.header-logo {
  font-size: $font-size-logo;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  
  .logo-icon {
    width: 1.9rem;
    height: 1.9rem;
    vertical-align: middle;
    transition: transform 0.3s ease;
    
    &:hover {
      transform: scale(1.1);
    }
  }
  
  .icon {
    @include icon(1.9rem, $color-black);
    vertical-align: middle;
  }
  
  span {
    margin-left: 1rem;
  }
}

.yin-header-nav {
  flex: 1;
}

/*搜索输入框*/
.header-search {
  margin: 0 20px;
  width: 100%;
  position: relative;
  
  &::v-deep input {
    text-indent: 5px;
    max-width: $header-search-max-width;
    min-width: $header-search-min-width;
    border-radius: $header-search-radius;
    box-shadow: none;
    background-color: $color-light-grey;
    color: $color-black;
  }
  
  .search-history-panel {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    z-index: 1000;
    max-height: 300px;
    overflow-y: auto;
    
    .search-history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      border-bottom: 1px solid #e4e7ed;
      font-size: 12px;
      color: #909399;
      background: #f5f7fa;
      
      span {
        font-weight: 500;
      }
    }
    
    .search-history-list {
      .search-history-item {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        cursor: pointer;
        transition: background-color 0.2s;
        
        &:hover {
          background-color: #f5f7fa;
        }
        
        i.el-icon-time {
          margin-right: 8px;
          color: #c0c4cc;
          font-size: 14px;
        }
        
        span {
          flex: 1;
          font-size: 14px;
          color: #606266;
        }
        
        .delete-btn {
          color: #c0c4cc;
          font-size: 12px;
          padding: 2px;
          border-radius: 2px;
          transition: all 0.2s;
          
          &:hover {
            color: #f56c6c;
            background-color: #fef0f0;
          }
        }
      }
    }
  }
  
  .search-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    cursor: pointer;
    transition: background-color 0.2s;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    i.el-icon-search {
      margin-right: 8px;
      color: #c0c4cc;
      font-size: 14px;
    }
    
    span {
      flex: 1;
      font-size: 14px;
      color: #606266;
    }
    
    .delete-btn {
      color: #c0c4cc;
      font-size: 12px;
      padding: 2px;
      border-radius: 2px;
      transition: all 0.2s;
      
      &:hover {
        color: #f56c6c;
        background-color: #fef0f0;
      }
    }
  }
}

/*用户*/
.user-wrap {
  position: relative;
  display: flex;
  align-items: center;

  .user {
    width: $header-user-width;
    height: $header-user-width;
    border-radius: $header-user-radius;
    margin-right: $header-user-margin;
    cursor: pointer;
  }
}
</style>
