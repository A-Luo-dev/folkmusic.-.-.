<template>
  <yin-login-logo></yin-login-logo>
  <div class="sign">
    <div class="sign-head">
      <span>用户注册</span>
    </div>
    <el-form
      ref="signUpForm"
      label-width="70px"
      status-icon
      :model="registerForm"
      :rules="SignUpRules"
    >
      <!-- 用户名 -->
      <el-form-item prop="username" label="用户名">
        <el-input v-model="registerForm.username" placeholder="用户名"></el-input>
      </el-form-item>

      <!-- 密码 -->
      <el-form-item prop="password" label="密码">
        <el-input
          type="password"
          placeholder="密码"
          v-model="registerForm.password"
        ></el-input>
      </el-form-item>

      <!-- 性别 -->
      <el-form-item prop="sex" label="性别">
        <el-radio-group v-model="registerForm.sex">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="0">女</el-radio>
          <el-radio :label="2">保密</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 生日 -->
      <el-form-item prop="birth" label="生日">
        <el-date-picker
          v-model="registerForm.birth"
          type="date"
          placeholder=""
          style="width: 100%"
        />
      </el-form-item>

      <!-- 按钮区域 -->
      <el-form-item class="sign-btn">
        <el-button @click="goBackRegist()">取消</el-button>
        <el-button type="primary" @click="handleSignUp()">确定</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, getCurrentInstance } from "vue";
import mixin from "@/mixins/mixin";
import YinLoginLogo from "@/components/layouts/YinLoginLogo.vue";
import { HttpManager } from "@/api";
import { RouterName, NavName, SignUpRules } from "@/enums";

export default defineComponent({
  components: {
    YinLoginLogo,
  },
  setup() {
    const { proxy } = getCurrentInstance();
    const { routerManager, changeIndex } = mixin();

    const registerForm = reactive({
      username: "",
      password: "",
      sex: 1,
      birth: null,
    });

    async function goBackRegist() {
      changeIndex(NavName.SignIn);
      routerManager(RouterName.SignIn, { path: RouterName.SignIn });
    }

    async function handleSignUp() {
      let canRun = true;
      (proxy.$refs["signUpForm"] as any).validate((valid) => {
        if (!valid) return (canRun = false);
      });
      if (!canRun) return;

      try {
        const { username, password, sex, birth } = registerForm;
        const result = (await HttpManager.SignUp({
          username,
          password,
          sex,
          birth,
        } as any)) as ResponseBody;

        (proxy as any).$message({
          message: result.message,
          type: result.type,
        });

        if (result.success) {
          changeIndex(NavName.SignIn);
          routerManager(RouterName.SignIn, { path: RouterName.SignIn });
        }
      } catch (error) {
        console.error(error);
      }
    }

    return {
      SignUpRules,
      registerForm,
      handleSignUp,
      goBackRegist,
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/sign.scss";
</style>
