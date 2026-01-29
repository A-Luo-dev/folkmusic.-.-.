<template>
  <div class="play-list-container">
    <yin-nav :styleList="songStyle" :activeName="activeName" @click="handleChangeView"></yin-nav>
    <play-list :playList="data" path="song-sheet-detail"></play-list>
    <el-pagination
      class="pagination"
      background
      layout="total, prev, pager, next"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="allPlayList.length"
      @current-change="handleCurrentChange"
    >
    </el-pagination>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from "vue";
import YinNav from "@/components/layouts/YinNav.vue";
import PlayList from "@/components/PlayList.vue";
import { SONGSTYLE } from "@/enums";
import { HttpManager } from "@/api";

export default defineComponent({
  components: {
    YinNav,
    PlayList,
  },
  setup() {
    const activeName = ref("全部歌单");
    const pageSize = ref(15); // 页数
    const currentPage = ref(1); // 当前页
    const songStyle = ref(SONGSTYLE); // 歌单导航栏类别
    const allPlayList = ref([]); // 歌单
    const data = computed(() => allPlayList.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value));

    // 获取全部歌单
    async function getSongList() {
      allPlayList.value = ((await HttpManager.getSongList()) as ResponseBody).data;
      currentPage.value = 1;
    }
    // 通过类别获取歌单
    async function getSongListOfStyle(style) {
      allPlayList.value = ((await HttpManager.getSongListOfStyle(style)) as ResponseBody).data;
      currentPage.value = 1;
    }

    try {
      getSongList();
    } catch (error) {
      console.error(error);
    }

    // 获取歌单
    async function handleChangeView(item) {
      activeName.value = item.name;
      allPlayList.value = [];
      try {
        if (item.name === "全部歌单") {
          await getSongList();
        } else {
          await getSongListOfStyle(item.name);
        }
      } catch (error) {
        console.error(error);
      }
    }
    // 获取当前页
    function handleCurrentChange(val) {
      currentPage.value = val;
    }
    return {
      activeName,
      songStyle,
      pageSize,
      currentPage,
      allPlayList,
      data,
      handleChangeView,
      handleCurrentChange,
    };
  },
});
</script>


<style lang="scss" scoped>
.play-list-container {
  position: relative; // 保证内部定位有效

  .pagination {
    display: flex;
    justify-content: center; // 分页按钮居中
    margin-top: 20px;        // 分页与列表内容间距
    margin-bottom: 120px;    // 与底部播放器保持安全间距
    flex-wrap: wrap;          // 小屏幕换行防止溢出

    @media screen and (max-width: 768px) {
      margin-bottom: 150px;  // 小屏幕适当加大底部间距
      font-size: 12px;       // 小屏幕按钮略小
    }

    @media screen and (min-width: 1200px) {
      margin-bottom: 100px;  // 大屏幕底部间距稍小
      font-size: 14px;
    }
  }
}
</style>

