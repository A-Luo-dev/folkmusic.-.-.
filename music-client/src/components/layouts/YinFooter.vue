<!-- <template>
  <div class="yin-footer">
    <p v-for="(item, index) in footerList" :key="index">
      {{ item }}
    </p>
  </div>
</template>

<script lang="ts">
import { defineComponent, readonly } from "vue";

export default defineComponent({
  setup() {
    const footerList = readonly([
      "关于 | 帮助 | 条款 | 反馈",
      "Copyright © 2025 Luo-Di",
    ]);

    return { footerList };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

.yin-footer {
  position: absolute; // 贴底
  bottom: 70px;       // 离底部一点间距，看起来更呼吸
  left: 0;
  width: 100%;
  text-align: center;
  color: white;       // 在背景图上清晰可见
  font-size: 14px;
  line-height: 1.5;   // 调整行高，让两行文字更舒展
  background: none;   // ❌去掉背景矩形
  padding: 0;         // ❌去掉额外的上下 padding
}

.yin-footer p {
  margin: 2px 0;      // 控制两行间距，避免挤在一起
  height: auto;       // ❌不要强行固定高度
}

</style> -->
