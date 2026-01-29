import axios from "axios";
import router from "@/router";
import { ElMessage } from "element-plus";

const BASE_URL = process.env.NODE_HOST;

axios.defaults.timeout = 1000000; // 超时时间
axios.defaults.withCredentials = true; // 允许跨域
axios.defaults.baseURL = BASE_URL;

// Content-Type 响应头
axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded;charset=UTF-8";

// 响应拦截器
axios.interceptors.response.use(
  (response) => {
    if (response && response.status === 200) {
      return Promise.resolve(response);
    } else {
      ElMessage.error("服务器返回异常");
      return Promise.reject(response);
    }
  },
  (error) => {
    if (error.code === "ECONNABORTED" || error.message.includes("timeout")) {
      // 请求超时
      ElMessage.error("请求超时，请稍后再试");
    } else if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.warning("未授权，请重新登录");
          router.replace({ path: "/" });
          break;
        case 403:
          ElMessage.warning("登录已过期，请重新登录");
          setTimeout(() => {
            router.replace({ path: "/" });
          }, 1000);
          break;
        case 404:
          ElMessage.error("请求接口不存在 (404)");
          break;
        case 500:
          ElMessage.error("服务器内部错误 (500)");
          break;
        default:
          ElMessage.error(`请求错误: ${error.response.status}`);
      }
    } else {
      // 没有响应（断网 / 服务器挂掉）
      ElMessage.error("网络异常，请检查网络连接");
    }
    return Promise.reject(error);
  }
);

export function getBaseURL() {
  return BASE_URL;
}

// get 请求
export function get(url, params?: object) {
  return new Promise((resolve, reject) => {
    axios.get(url, { params }).then(
      (response) => resolve(response.data),
      (error) => reject(error)
    );
  });
}

// post 请求
export function post(url, data = {}) {
  return new Promise((resolve, reject) => {
    axios.post(url, data).then(
      (response) => resolve(response.data),
      (error) => reject(error)
    );
  });
}

// delete 请求
export function deletes(url, data = {}) {
  return new Promise((resolve, reject) => {
    axios.delete(url, { data }).then(
      (response) => resolve(response.data),
      (error) => reject(error)
    );
  });
}

// put 请求
export function put(url, data = {}) {
  return new Promise((resolve, reject) => {
    axios.put(url, data).then(
      (response) => resolve(response.data),
      (error) => reject(error)
    );
  });
}
