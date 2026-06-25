import type { UserInfo } from "@/types/UserInfo";

import { defineStore } from "pinia";
import { computed, readonly, ref } from "vue";

import { getUserInfo } from "@/api/userinfo-client";
import { Role } from "@/types/Role";

function isRole(value: string): value is Role {
  return Object.values(Role).includes(value as Role);
}

export const useUserInfoStore = defineStore("userInfo", () => {
  const internalUserInfo = ref<UserInfo | null>(null);
  const userInfo = readonly(internalUserInfo);

  async function fetchUserInfo(): Promise<void> {
    try {
      internalUserInfo.value = await getUserInfo();
    } catch {
      // SSO is optional for this tool (e.g. local "no-security" operation without a gateway).
      // Failing to load the user info must not block the application.
      internalUserInfo.value = null;
    }
  }

  const currentRoles = computed(() => {
    const allUserInfoRoles =
      Object.values(internalUserInfo.value?.resource_access ?? {})[0]?.roles ??
      [];
    return allUserInfoRoles.filter(isRole);
  });

  return { userInfo, currentRoles, fetchUserInfo };
});
