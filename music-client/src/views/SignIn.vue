<template>
  <yin-login-logo></yin-login-logo>
  
  <div class="sign">
    <div class="sign-head">
      <span>帐号登录</span>
    </div>
    <el-form ref="signInForm" status-icon :model="registerForm" :rules="SignInRules">
      <el-form-item prop="username" label="用户名">
        <el-input placeholder="用户名" v-model="registerForm.username"></el-input>
      </el-form-item>
      <el-form-item prop="password" label="  密码">
        <el-input
          type="password"
          placeholder="密码"
          v-model="registerForm.password"
          @keyup.enter="handleLoginIn"
        ></el-input>
      </el-form-item>

      <!-- 🔹 新增验证码 -->
      <el-form-item prop="code" label="验证码">
        <div class="captcha-box">
          <el-input placeholder="请输入验证码" v-model="registerForm.code"></el-input>
          <img
            :src="captchaImg"
            class="captcha-img"
            @click="getCaptcha"
            alt="验证码"
          />
        </div>
      </el-form-item>

      <!-- 上方按钮区 -->
      <el-form-item class="sign-btn">
        <el-button type="primary" @click="handleLoginIn">登录</el-button>
        <el-button @click="handleEmail">邮箱登录</el-button>
      </el-form-item>

      <!-- 下方文字链接 -->
      <div class="sign-links">
        <span class="link" @click="handleSignUp">注册</span>
        <span class="link" @click="handleForgotPassword">忘记密码</span>
      </div>
    </el-form>
  </div>
</template>


<script lang="ts">
import { defineComponent, reactive, getCurrentInstance, onMounted, ref } from "vue";
import mixin from "@/mixins/mixin";
import YinLoginLogo from "@/components/layouts/YinLoginLogo.vue";
import { HttpManager } from "@/api";
import { NavName, RouterName } from "@/enums"; // ⚡️ 这里我去掉了原先的 SignInRules 引入，因为我们要自己定义新的规则

export default defineComponent({
  components: {
    YinLoginLogo,
  },
  setup() {
    const { proxy } = getCurrentInstance();
    const { routerManager, changeIndex } = mixin();

    // 登录表单数据
    const registerForm = reactive({
      username: "",
      password: "",
      code: "",   // 🔹 验证码
      key: "",    // 🔹 验证码唯一 key
    });

    // 验证码图片
    const captchaImg = ref("");

    // 表单校验规则
    const SignInRules = {
      username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
      password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
    };

      // 获取验证码
      async function getCaptcha() {
        try {
          const res = (await HttpManager.getCaptcha()) as {
            code: number;
            msg: string;
            data: { key: string; img: string };
          };

          registerForm.key = res.data.key;
          captchaImg.value = "data:image/png;base64," + res.data.img; // base64 图像
        } catch (err) {
          console.error("获取验证码失败", err);
        }
      }

    // 登录方法
    async function handleLoginIn() {
      let canRun = true;
      (proxy.$refs["signInForm"] as any).validate((valid) => {
        if (!valid) return (canRun = false);
      });
      if (!canRun) return;

      try {
        const { username, password, code, key } = registerForm;
        // 🔹 增加 code 和 key 一起传给后端
        const result = (await HttpManager.signIn({ username, password, code, key })) as ResponseBody;

        (proxy as any).$message({
          message: result.message,
          type: result.type,
        });

        if (result.success) {
          const userId = result.data[0].id;
          const username = result.data[0].username;
          const avatar = result.data[0].avator;

          // 写入 Vuex
          proxy.$store.commit("setUserId", userId);
          proxy.$store.commit("setUsername", username);
          proxy.$store.commit("setUserPic", avatar);
          proxy.$store.commit("setToken", true);

          // ✅ 写入 localStorage
          localStorage.setItem("userId", userId);
          localStorage.setItem("username", username);
          localStorage.setItem("avatar", avatar);
          // 🔹 新增 consumerId
          localStorage.setItem("consumerId", userId); 

          // 跳转首页
          changeIndex(NavName.Home);
          routerManager(RouterName.Home, { path: RouterName.Home });
        } else {
          getCaptcha(); // 登录失败刷新验证码
        }
      } catch (error) {
        console.error(error);
        getCaptcha();
      }
    }

    function handleSignUp() {
      changeIndex(NavName.SignUp);
      routerManager(RouterName.SignUp, { path: RouterName.SignUp });
    }

    function handleForgotPassword() {
      routerManager(RouterName.ForgotPassword, { path: RouterName.ForgotPassword });
    }
    function handleEmail() {
      routerManager(RouterName.loginByemail, { path: RouterName.loginByemail });
    }

    onMounted(() => {
      getCaptcha();
    });

    return {
      registerForm,
      SignInRules,
      handleLoginIn,
      handleForgotPassword,
      handleEmail,
      handleSignUp,
      captchaImg,
      getCaptcha,
    };
  },
});
</script>


<style lang="scss" scoped>
@import "@/assets/css/sign.scss";

.captcha-box {
  display: flex;
  align-items: center;
}
.captcha-img {
  margin-left: 10px;
  height: 40px;
  cursor: pointer;
  border: 1px solid #ddd;
}
</style>
