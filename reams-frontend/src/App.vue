<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
:root {
  --el-font-size-base: 17px;
  --el-font-size-small: 16px;
  --el-font-size-extra-small: 15px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 17px;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
  font-size: 17px;
  line-height: 1.6;
  color: #1f2937;
}

body {
  font-size: 17px;
  line-height: 1.6;
  background-color: #f5f5f5;
}

button,
input,
textarea,
select {
  font: inherit;
}

.el-button,
.el-pagination,
.el-form-item__label,
.el-radio__label,
.el-checkbox__label,
.el-dialog__title,
.el-drawer__title,
.el-descriptions__label,
.el-descriptions__content,
.el-table,
.el-tag,
.el-input__inner,
.el-textarea__inner,
.el-select__placeholder,
.el-select__selected-item,
.el-menu-item,
.el-sub-menu__title,
.el-dropdown-menu__item,
.el-statistic__content,
.el-card__header,
.el-empty__description {
  font-size: 16px;
}
</style>
