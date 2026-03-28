<template>
  <div class="forum">
    <!-- 左边：帖子列表 -->
    <div class="post-list">
      <div class="list-header">
        <h3>社区帖子</h3>
        <el-input
          v-model="searchKeyword"
          placeholder="🔍搜索帖子..."
          clearable
          size="small"
          class="search-box"
          suffix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
      </div>

      <!-- 帖子列表：去掉 el-scrollbar，直接展示 -->
      <div class="list-body">
        <el-card
          v-for="post in posts"
          :key="post.postId"
          class="post-item"
          :class="{ selected: selectedPostId === post.postId }"
          shadow="hover"
          @click="selectPost(post.postId)"
        >
          <div class="post-header">
            <h4>{{ post.title }} <span class="author-badge">({{ post.username || '匿名用户' }})</span></h4>
            <div class="post-meta">
              <span class="time">{{ formatTime(post.createdAt) }}</span>
            </div>
          </div>
          <p class="post-preview">{{ post.content.slice(0, 40) }}...</p>
          <div class="post-tags" v-if="post.tags && post.tags.length > 0">
            <el-tag
              v-for="(tag, idx) in post.tags"
              :key="idx"
              :type="tagColor(tag)"
              effect="light"
              size="small"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
        </el-card>
      </div>

      <!-- 分页控件 -->
      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          @current-change="handleCurrentChange"
          v-model:current-page="currentPage"
        />
      </div>
    </div>



    <!-- 右边：发帖 + 帖子详情 -->
    <div class="main-content">
      <!-- 发帖入口 -->
      <el-card class="post-create" shadow="always">
        <h3>发布新帖子</h3>
        <el-input
          v-model="newPost.title"
          placeholder="请输入帖子标题"
          clearable
          class="mb-2"
        />
        <el-input
          v-model="newPost.content"
          type="textarea"
          placeholder="分享你的民谣故事..."
          :rows="5"
          class="mb-2"
        />

        <!-- 标签选择区 -->
        <div class="tags-area">
          <p class="hint">提示：最多可选三个</p>
          <el-checkbox-group v-model="newPost.tags" @change="limitTags">
            <el-checkbox label="原创"></el-checkbox>
            <el-checkbox label="吉他弹唱"></el-checkbox>
            <el-checkbox label="现场演出"></el-checkbox>
            <el-checkbox label="歌词分享"></el-checkbox>
            <el-checkbox label="民谣旅行"></el-checkbox>
          </el-checkbox-group>
        </div>

        <!-- 附件上传区 -->
        <div class="attachments-area">
          <p class="hint">附件上传（可选）</p>
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :file-list="newPost.attachments"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemoveAttachment"
            multiple
            :limit="5"
            accept="image/*,audio/*,video/*,.pdf,.doc,.docx,.txt"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持图片、音频、视频、文档等格式，单个文件不超过50MB，最多5个文件
              </div>
            </template>
          </el-upload>
        </div>

        <div class="actions">
          <el-button type="primary" @click="createPost">发布</el-button>
        </div>
      </el-card>

      <!-- 帖子详情 -->
      <el-card v-if="currentPost" class="post-detail" shadow="always">
        <div class="post-header">
          <h3>{{ currentPost.post.title }}</h3>
          <div class="post-meta">
            <span class="author">by {{ currentPost.username || '匿名用户' }}</span>
            <span class="time">{{ formatTime(currentPost.post.createdAt) }}</span>
          </div>
          <div class="tags" v-if="currentPost.post.tags && currentPost.post.tags.trim()">
            <el-tag
              v-for="(tag, idx) in parseTags(currentPost.post.tags)"
              :key="idx"
              :type="tagColor(tag)"
              effect="light"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <p>{{ currentPost.post.content }}</p>

        <div class="attachments" v-if="currentPost.attachments?.length">
          <h4>附件</h4>
          <div v-for="a in currentPost.attachments" :key="a.attachmentId" class="attachment-item">
            <el-link :href="a.url" target="_blank" class="attachment-link">
              <i :class="getAttachmentIcon(a.type)"></i>
              {{ getAttachmentName(a) }}
            </el-link>
            <span class="attachment-type">{{ a.type }}</span>
          </div>
        </div>

        <h4>评论区</h4>
        <div
          v-for="c in currentPost.comments"
          :key="c.commentId"
          class="comment-item"
        >
          <el-card shadow="never" class="comment-card">
            {{ c.content }} - 用户{{ c.userId }}
          </el-card>
        </div>

        <div class="comment-create">
          <el-input
            v-model="newComment"
            placeholder="写下你的评论..."
            clearable
          />
          <el-button type="primary" class="ml-2" @click="addComment">
            评论
          </el-button>
        </div>
      </el-card>


    </div>
  </div>
</template>

<script>
import axios from "axios";
import { ElMessage } from "element-plus";

axios.interceptors.request.use(
  (config) => {
    const userId = localStorage.getItem("userId");
    if (userId) {
      config.headers["X-User-Id"] = userId;
    } else {
      delete config.headers["X-User-Id"]; // ⚠️ 关键：未登录删除 Header
    }
    return config;
  },
  (error) => Promise.reject(error)
);



export default {
  name: "Forum",
  data() {
    return {
      posts: [],
      currentPost: null,
      newPost: { title: "", content: "", tags: [], attachments: [] },
      newComment: "",
      userId: null,
      searchKeyword: "",
      selectedPostId: null,
      total: 0, // 用于记录数据库中符合条件的帖子总数

    // 🔑 分页参数
    currentPage: 1,   // 当前页
    pageSize: 5,      // 每页条数
    
    // 文件上传配置
    uploadUrl: "http://localhost:8888/attachments/upload",
    uploadHeaders: {},

    };
  },
computed: {

},
methods: {
  //点击搜索或者回车会刷新一次帖子列表
  handleSearch() {
    this.currentPage = 1;
    this.fetchPosts();
  },

  //点击分页会刷新一次帖子列表
  handleCurrentChange() {
    this.fetchPosts();
  },
  

  async fetchPosts() {
    try {
      const res = await axios.get("/posts", {
        params: {
          keyword: this.searchKeyword.trim(), // 搜索关键字
          pageNum: this.currentPage,          // 当前页
          pageSize: this.pageSize             // 每页条数
        }
      });
      console.log("获取到的帖子分页数据:", res.data);
      this.posts = res.data.records;
      this.total = res.data.total;
    } catch (err) {
      console.error("获取帖子列表失败:", err);
      ElMessage.error("获取帖子列表失败");
    }
  },

  async selectPost(postId) {
    try {
      const res = await axios.get(`/posts/${postId}`);
      console.log("获取到的帖子详情:", res.data);
      this.currentPost = res.data || {};
    } catch (err) {
      console.error("获取帖子详情失败:", err);
      ElMessage.error("获取帖子详情失败");
    }
  },

  async createPost() {
    // 检查登录状态
    const token = this.$store.getters.token;
    const userId = this.$store.getters.userId;
    
    if (!token || !userId) {
      ElMessage.warning("请先登录！");
      return;
    }
    if (!this.newPost.title.trim()) {
      ElMessage.warning("请输入帖子标题");
      return;
    }
    if (!this.newPost.content.trim()) {
      ElMessage.warning("帖子内容不能为空");
      return;
    }
    try {
      console.log("发送帖子数据:", {
        title: this.newPost.title,
        content: this.newPost.content,
        tags: this.newPost.tags,
        attachments: this.newPost.attachments,
      });
      
      const response = await axios.post("/posts", {
        title: this.newPost.title,
        content: this.newPost.content,
        tags: this.newPost.tags,
        attachments: this.newPost.attachments,
      });
      
      console.log("发帖响应:", response.data);
      ElMessage.success("帖子发布成功！");
      this.newPost = { title: "", content: "", tags: [], attachments: [] };
      this.fetchPosts();
    } catch (err) {
      console.error("发布帖子失败:", err);
      ElMessage.error("发布失败，请稍后重试");
    }
  },

  async addComment() {
    // 检查登录状态
    const token = this.$store.getters.token;
    const userId = this.$store.getters.userId;
    
    if (!token || !userId) {
      ElMessage.error("请先登录！");
      return;
    }
    if (!this.newComment.trim()) {
      ElMessage.error("评论不能为空！");
      return;
    }
    try {
      const resp = await axios.post("/comments", {
        postId: this.currentPost.post.postId,
        parentId: 0,
        content: this.newComment,
      });
      if (resp.data.success) {
        ElMessage.success("评论成功！");
        this.newComment = "";
        this.selectPost(this.currentPost.post.postId); // 刷新评论
      }
    } catch (e) {
      console.error("评论失败:", e);
      ElMessage.error("评论失败，请稍后再试");
    }
  },

  async deletePost(postId) {
    // 检查登录状态
    const token = this.$store.getters.token;
    const userId = this.$store.getters.userId;
    
    if (!token || !userId) {
      ElMessage.warning("请先登录！");
      return;
    }
    try {
      await axios.delete(`/posts/${postId}`);
      ElMessage.success("帖子删除成功");
      this.fetchPosts();
    } catch (err) {
      console.error("删除帖子失败:", err);
      ElMessage.error("删除失败");
    }
  },

  async deleteComment(commentId) {
    // 检查登录状态
    const token = this.$store.getters.token;
    const userId = this.$store.getters.userId;
    
    if (!token || !userId) {
      ElMessage.warning("请先登录！");
      return;
    }
    try {
      await axios.delete(`/comments/${commentId}`);
      ElMessage.success("评论删除成功");
      this.selectPost(this.currentPost.post.postId);
    } catch (err) {
      console.error("删除评论失败:", err);
      ElMessage.error("删除失败");
    }
  },

  // ✅ 新增方法：登出清理
  logout() {
    // 清理 localStorage
    localStorage.removeItem("userId");

    // 清理组件状态
    this.userId = null;
    this.currentPost = null;
    this.selectedPostId = null;

    // 如果有 Vuex/Pinia 状态，也同步清理
    if (this.$store) {
      this.$store.commit("setUserId", null);
    }

    ElMessage.success("已退出登录");

    // 重新拉取帖子列表
    this.fetchPosts();
  },
      tagColor(tag) {
      switch (tag) {
        case "原创": return "success";   // 绿色
        case "吉他弹唱": return "warning"; // 橙色
        case "现场演出": return "danger";   // 红色
        case "歌词分享": return "info";     // 蓝色
        case "民谣旅行": return "";         // 默认灰
        default: return "";
      }
    },
    
    formatTime(timeStr) {
      if (!timeStr) return '';
      try {
        const date = new Date(timeStr);
        if (isNaN(date.getTime())) return '';
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
      } catch (e) {
        console.error('时间格式化失败:', e);
        return '';
      }
    },
    
    limitTags() {
      if (this.newPost.tags.length > 3) {
        this.newPost.tags = this.newPost.tags.slice(0, 3);
        ElMessage.warning("最多只能选择3个标签");
      }
    },
    
    parseTags(tagsStr) {
      if (!tagsStr || !tagsStr.trim()) return [];
      try {
        return JSON.parse(tagsStr);
      } catch (e) {
        console.error("解析标签失败:", e);
        return [];
      }
    },
    
    // 文件上传相关方法
    beforeUpload(file) {
      const isValidType = this.checkFileType(file.type);
      const isLt50M = file.size / 1024 / 1024 < 50;
      
      if (!isValidType) {
        ElMessage.error('不支持的文件类型！');
        return false;
      }
      if (!isLt50M) {
        ElMessage.error('文件大小不能超过 50MB!');
        return false;
      }
      return true;
    },
    
    checkFileType(type) {
      const allowedTypes = [
        'image/jpeg', 'image/png', 'image/gif', 'image/webp',
        'audio/mpeg', 'audio/mp3', 'audio/wav', 'audio/ogg',
        'video/mp4', 'video/avi', 'video/mov',
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'text/plain'
      ];
      return allowedTypes.includes(type);
    },
    
    handleUploadSuccess(response, file, fileList) {
      console.log('文件上传成功响应:', response);
      console.log('上传的文件:', file);
      if (response.success) {
        this.newPost.attachments.push({
          name: file.name,
          url: response.url,
          type: response.type || this.getFileType(file.name),
          attachmentId: response.attachmentId,
          fileName: response.fileName
        });
        console.log('当前附件列表:', this.newPost.attachments);
        ElMessage.success('文件上传成功');
      } else {
        console.error('文件上传失败:', response);
        ElMessage.error('文件上传失败');
      }
    },
    
    handleRemoveAttachment(file, fileList) {
      console.log('移除文件:', file);
      this.newPost.attachments = this.newPost.attachments.filter(
        item => item.name !== file.name
      );
    },
    
    getFileType(filename) {
      const ext = filename.split('.').pop().toLowerCase();
      if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return 'image';
      if (['mp3', 'wav', 'ogg', 'm4a'].includes(ext)) return 'audio';
      if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return 'video';
      if (['pdf'].includes(ext)) return 'pdf';
      if (['doc', 'docx'].includes(ext)) return 'document';
      return 'file';
    },
    
    getAttachmentIcon(type) {
      switch (type) {
        case 'image': return 'el-icon-picture';
        case 'audio': return 'el-icon-headset';
        case 'video': return 'el-icon-video-camera';
        case 'pdf': return 'el-icon-document';
        case 'document': return 'el-icon-document';
        default: return 'el-icon-document';
      }
    },
    
    getAttachmentName(attachment) {
      // 从URL中提取文件名，或使用默认名称
      if (attachment.url) {
        const parts = attachment.url.split('/');
        return parts[parts.length - 1] || '附件';
      }
      return '附件';
    },
},
  mounted() {
    // 从store获取用户信息
    this.userId = this.$store.getters.userId;
    console.log("Forum页面加载，用户ID:", this.userId);
    
    // 设置上传请求头
    this.uploadHeaders = {
      'X-User-Id': this.userId || ''
    };
    console.log("上传请求头:", this.uploadHeaders);
    
    this.fetchPosts();
  },
};
</script>


<style lang="scss" scoped>
@import "@/assets/css/forum.scss";
</style>
