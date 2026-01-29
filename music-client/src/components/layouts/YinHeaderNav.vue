<template>
  <ul class="yin-header-nav">
    <li :class="{ active: item.name === activeName }" v-for="item in styleList" :key="item.path" @click="handleChangeView(item)">
      {{ item.name }}
    </li>
  </ul>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance } from "vue";

export default defineComponent({
  props: {
    styleList: Array,
    activeName: String,
  },
  emits: ["click"],
  setup() {
    const { proxy } = getCurrentInstance();

    function handleChangeView(item) {
      proxy.$emit("click", item.path, item.name);
    }
    return {
      handleChangeView,
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";

li {
  margin: $header-nav-margin;
  padding: $header-nav-padding;
  line-height: 3.3rem;
  color: $color-grey;
  border-bottom: none;
  cursor: pointer;
  transition: all 0.3s ease; // 加个平滑过渡
}

li.active {
  color: $color-black;
  font-weight: 600;
  border-bottom: 5px solid $color-black;
}

/* 👇 鼠标悬停效果 */
li:hover {
  color: $color-primary; // 用主题色或者直接 #409EFF
  transform: translateY(-2px); // 轻微上浮
  border-bottom: 3px solid $color-primary; // 悬停时底部加高亮线
}

</style>
