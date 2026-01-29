<template>
  <div class="login-logo">
    <!-- 页脚信息 -->
    <div class="login-footer">
      <p>关于 | 帮助 | 条款 | 反馈</p>
      <p>Copyright © 2025 Luo-Di</p>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

/* 背景图容器 */
.login-logo {
  background: url("http://127.0.0.1:9000/user01/img/backgroundImg/login.jpg") no-repeat center center;
  background-size: cover;
  height: 82vh; /* 占满整屏 */
  width: 100vw;
  overflow: hidden;
  position: relative; /* 让 footer 可以绝对定位 */
  @include layout(center, center);
}

/* footer 样式 */
.login-footer {
  position: absolute;
  bottom: 10px; /* 贴底 */
  left: 0;
  width: 100%;
  text-align: center;
  font-size: 14px;
  color: white; /* 确保在深色背景上可见 */

  p {
    margin: 4px 0;
    line-height: 1.4;
  }
}
</style>
