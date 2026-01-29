<template>
  <div class="container">
    <!-- 左侧榜单切换 -->
    <div class="container-left">
      <div class="rank-aside">
        <div class="rank-type">
          <span @click="selectMenu(0)" :class="{ active: type === 0 }">Top榜</span>
          <span @click="selectMenu(1)" :class="{ active: type === 1 }">特色榜</span>
          <span @click="selectMenu(2)" :class="{ active: type === 2 }">场景榜</span>
        </div>
      </div>
    </div>

    <!-- 🔹右侧整体区域（左右分栏：大榜单 + 窄猜你喜欢） -->
    <div class="container-right">
      <!-- 左边：榜单详情 -->
      <div class="rank-detail">
        <div class="cover-info">
          <!-- 封面 -->
          <div class="el-image">
            <el-image
              style="width: 100%; height: 100%"
              :src="playlist?.coverImgUrl"
              fit="cover"
            />
          </div>

          <!-- 信息框 -->
          <el-card class="box-card">
            <h1>{{ playlist?.name }}</h1>
            <div class="userinfo">
              <div class="update-time">更新时间：{{ formatDate }}</div>
            </div>
            <div class="desc">
              <h2>简介</h2>
              <span>{{ playlist?.description }}</span>
            </div>
          </el-card>
        </div>

        <!-- 歌曲列表 -->
        <div class="songlist-wrapper">
          <SongList :songList="playlist?.songs || []" :isRank="true" />
        </div>
      </div>

<!-- 右边：猜你喜欢（独立瘦子栏） -->
<div class="recommend-box">
  <el-card class="recommend-card">
    <h2 class="recommend-title">猜你喜欢</h2>
    <ul class="recommend-list">
      <li
        v-for="song in recommendSongs"
        :key="song.id"
        @click="playFromRecommend(song)"
        class="recommend-item"
      >
        <span class="song-name">{{ song.name }}</span>
      </li>
    </ul>
  </el-card>
</div>

    </div>
  </div>
</template>


<script setup lang="ts">
import { useStore } from "vuex";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import SongList from "@/components/SongList.vue";
import { HttpManager } from "@/api";
import { ElMessage } from "element-plus";

interface RankItem {
  id: number;
  name: string;
  coverImgUrl: string;
  type: string;
  description?: string;
  songs?: any[];
}

interface Playlist {
  name: string;
  coverImgUrl: string;
  description?: string;
  songs: any[];
}
const store = useStore();
const router = useRouter();
const list = ref<RankItem[]>([]);
const type = ref<number>(0);
const listTop = ref<RankItem[]>([]);
const listFeature = ref<RankItem[]>([]);
const listOuther = ref<RankItem[]>([]);
const playlist = ref<Playlist | null>(null);
const uid = ref<number | null>(null);
const today = new Date();
const formatDate = `${today.getFullYear()}-${(today.getMonth()+1).toString().padStart(2,'0')}-${today.getDate().toString().padStart(2,'0')}`;

// 🔹猜你喜欢歌曲
const recommendSongs = ref<any[]>([]);

function getCoverUrl(fileName: string) {
  if (!fileName) return "";
  return `http://localhost:8888${fileName}`;
}

// 🔹 回滚推荐歌曲播放逻辑
function playFromRecommend(song: any) {
  if (!song || !song.id) return;

  // 找到歌曲在推荐列表的索引
  const songIndex = recommendSongs.value.findIndex(s => s.id === song.id);
  if (songIndex === -1) return;

  // 更新全局播放列表和索引
  store.commit("setCurrentPlayList", recommendSongs.value);
  store.commit("setCurrentPlayIndex", songIndex);

  const currentSong = recommendSongs.value[songIndex];
  if (!currentSong.url) {
    ElMessage.warning("资源无效，无法播放");
    return;
  }

  // 🔹 调用全局播放器方法
  store.commit("setSongId", currentSong.id);
  store.commit("setSongUrl", currentSong.url);
  store.commit("setSongTitle", currentSong.name);
  store.commit("setSingerName", currentSong.name.split(" - ")[0]);
  store.commit("setSongPic", currentSong.pic);
  store.commit("setLyric", currentSong.lyric);
  store.commit("setIsPlay", true);

  // 🔹 使用全局播放器的 toPlay 方法逻辑记录点击
  const consumerId = localStorage.getItem("consumerId");
  if (consumerId) {
    HttpManager.addUserSongClick({
      userId: Number(consumerId),
      songId: currentSong.id
    }).catch(err => console.error("记录点击失败:", err));
  }
}


async function getListRank() {
  try {
    const result = await HttpManager.getAllRankList();
    if (!result || result.code !== 200 || !Array.isArray(result.data)) {
      ElMessage.error("请求数据失败");
      return;
    }
    listTop.value = result.data.filter((item: RankItem) => Number(item.type) === 0);
    listFeature.value = result.data.filter((item: RankItem) => Number(item.type) === 1);
    listOuther.value = result.data.filter((item: RankItem) => Number(item.type) === 2);

    selectMenu(type.value);

    if (!uid.value && list.value.length > 0) {
      selectID(list.value[0]);
    }
  } catch (err) {
    console.error("获取排行榜失败:", err);
  }
}

function selectMenu(menuType: number) {
  type.value = menuType;
  switch (menuType) {
    case 0: list.value = listTop.value; break;
    case 1: list.value = listFeature.value; break;
    case 2: list.value = listOuther.value; break;
  }
  if (list.value.length > 0) selectID(list.value[0]);
}

async function selectID(item: RankItem) {
  console.log("🔍 选中榜单 ID:", item);
  uid.value = item.id;
  playlist.value = {
    name: item.name,
    coverImgUrl: getCoverUrl(item.coverImgUrl),
    description: item.description,
    songs: [],
  };
  try {
    const result = await HttpManager.getMenuId({ id: item.id });
    console.log(`📂 榜单 ${item.id} 详情返回:`, result);

    if (result && result.code === 200) {
      playlist.value.songs = result.data.songs || [];
      console.log("🎶 榜单歌曲:", playlist.value.songs);

      // 🔹 只设置播放列表，不自动播放
      store.commit("setCurrentPlayList", playlist.value.songs);
      store.commit("setCurrentPlayIndex", 0); // 默认播放第一首
      // 移除自动播放：store.commit("setIsPlay", true);
    } else {
      ElMessage.error("获取榜单详情失败");
    }
  } catch (err) {
    console.error("❌ 获取榜单详情出错:", err);
    ElMessage.error("请求出错");
  }
  router.push({ path: "/hot-rank", query: { id: item.id } });
}


async function loadRecommend() {
  const consumerId = localStorage.getItem("consumerId");
  if (!consumerId) return;

  try {
    const clickList = await HttpManager.getUserRecentClick(Number(consumerId));
    console.log("获取到的点击记录:", clickList);

    if (!Array.isArray(clickList) || clickList.length === 0) {
      console.warn("用户没有点击记录");
      recommendSongs.value = [];
      return;
    }

    // 去重 + 限制最多 5 条
    const uniqueClicks = Array.from(
      new Map(clickList.map(item => [item.songId, item])).values()
    ).slice(0, 5);
    console.log("去重后的点击记录:", uniqueClicks);

    const songs: any[] = [];

    for (const item of uniqueClicks) {
      try {
        const songDetail = await HttpManager.getSongOfId(item.songId);
        if (songDetail?.code === 200 && songDetail.data) {
          const songInfo = Array.isArray(songDetail.data)
            ? songDetail.data[0]
            : songDetail.data;

          songs.push({
            id: songInfo.id,
            name: songInfo.name,  // 只保留歌曲名字
            url: songInfo.url,
            pic: songInfo.pic,
            lyric: songInfo.lyric,
          });
        }
      } catch (err) {
        console.error(`获取歌曲 ${item.songId} 详情出错:`, err);
      }
    }

    recommendSongs.value = songs;
    console.log("最终推荐歌曲列表:", recommendSongs.value);

  } catch (err) {
    console.error("加载猜你喜欢失败:", err);
    recommendSongs.value = [];
  }
}

onMounted(() => {
  getListRank();
  loadRecommend();
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/hotrank.scss";
</style>
