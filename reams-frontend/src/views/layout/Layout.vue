<template>
  <el-container class="layout-container">
    <el-container class="main-container">
      <!-- 主内容区 - 移除左侧边栏 -->
      <el-main>
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: 'Layout'
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 主容器 */
.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.el-main {
  background-color: #f0f2f5;
  padding: 0;
  overflow-y: auto;
  height: 100vh;
}
</style>
