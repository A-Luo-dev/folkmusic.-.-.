<template>
  <div class="content">
    <el-table
      highlight-current-row
      :data="dataList"
      @row-click="handleClick"
    >
    <!-- 名次列（只在排行榜用到） -->
    <el-table-column
      v-if="isRank"
      label="#"
      width="60"
      align="center"
    >
        <template #default="scope">
          <span
            :class="['rank-index', { top3: scope.row.index < 3 }]"
          >
            {{ scope.row.index + 1 }}
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="songName" label="歌曲" width="200" />
      <el-table-column prop="singerName" label="歌手"  width="160" />
      <el-table-column prop="introduction" label="专辑" />

      <el-table-column label="编辑" width="80" align="center">
        <template #default="scope">
          <el-dropdown>
            <el-icon @click="handleEdit(scope.row)">
              <MoreFilled />
            </el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  :icon="Download"
                  @click="
                    downloadMusic({
                      songUrl: scope.row.url,
                      songName: scope.row.name,
                    })
                  "
                >下载</el-dropdown-item>
                <el-dropdown-item
                  :icon="Delete"
                  v-if="show"
                  @click="deleteCollection({ id: scope.row.id })"
                >删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>


<script lang="ts">
import { defineComponent, getCurrentInstance, toRefs, computed, reactive } from "vue";
import { useStore } from "vuex";
import { MoreFilled, Delete, Download } from "@element-plus/icons-vue";

import mixin from "@/mixins/mixin";
import { HttpManager } from "@/api";
import { Icon } from "@/enums";

export default defineComponent({
  components: {
    MoreFilled,
  },
  props: {
    songList: {
      type: Array,
      default: () => []   // ✅ 确保默认就是空数组
    },
    show: {
      type: Boolean,
      default: false
    },
    isRank: {              // 👈 新增
      type: Boolean,
      default: false
    }
  },
  emits: ["changeData"],
  setup(props) {
    const { getSongTitle, getSingerName, playMusic, checkStatus, downloadMusic } = mixin();
    const { proxy } = getCurrentInstance();
    const store = useStore();

    const { songList } = toRefs(props);

    const iconList = reactive({
      dislike: Icon.Dislike,
      like: Icon.Like,
    });

    const songUrl = computed(() => store.getters.songUrl);
    const singerName = computed(() => store.getters.singerName);
    const songTitle = computed(() => store.getters.songTitle);

    // ✅ 防御性处理
    const dataList = computed(() => {
      const list: any[] = [];
      (songList.value || []).forEach((item: any, index: number) => {
        item["songName"] = getSongTitle(item.name);
        item["singerName"] = getSingerName(item.name);
        item["index"] = index;
        list.push(item);
      });
      return list;
    });

    function handleClick(row: any) {
      playMusic({
        id: row.id,
        url: row.url,
        pic: row.pic,
        index: row.index,
        name: row.name,
        lyric: row.lyric,
        currentSongList: songList.value || [],
      });
    }

    function handleEdit(row: any) {
      console.log("row", row);
    }

    const userId = computed(() => store.getters.userId);

    async function deleteCollection({ id }: { id: number }) {
      if (!checkStatus()) return;

      const result = (await HttpManager.deleteCollection(userId.value, id)) as ResponseBody;
      (proxy as any).$message({
        message: result.message,
        type: result.type,
      });

      if (result.data === false) proxy?.$emit("changeData", result.data);
    }

    return {
      dataList,
      iconList,
      Delete,
      Download,
      songUrl,
      singerName,
      songTitle,
      handleClick,
      handleEdit,
      downloadMusic,
      deleteCollection,
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

.content {
  background-color: $color-white;
  border-radius: $border-radius-songlist;
  padding: 10px;
}

.content:deep(.el-table__row.current-row) {
  color: $color-black;
  font-weight: bold;
}

.content:deep(.el-table__row) {
  cursor: pointer;
}

.rank-index {
  font-weight: bold;
  color: #666;
  display: inline-block;
  width: 26px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  border-radius: 50%;   // 👈 圆形
  background-color: transparent; // 默认无背景
}

.rank-index.top3 {
  background-color: #ff4d4f;  // 红色圆圈
  color: #fff;                // 白色字体
}
</style>

