/* eslint-disable */
declare module "*.vue" {
  import type { DefineComponent } from "vue";
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

declare module "@vue/runtime-core" {
  import type { Router, RouteLocationNormalizedLoaded } from "vue-router";

  interface ComponentCustomProperties {
    $router: Router;
    $route: RouteLocationNormalizedLoaded;
  }
}

declare global {
  interface ResponseBody {
    code: string;
    success: boolean;
    message: string;
    type: string;
    data?: any;
  }
}

export {};
