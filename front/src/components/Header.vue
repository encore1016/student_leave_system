<template>
  <div class="header">
    <div class="title">
      学生请假管理系统
    </div>

    <div class="user-info">
      欢迎你，{{ username }}
    </div>
  </div>
</template>

<script>
export default {
  name: "Header",
  data() {
    return {
      username: "admin"
    }
  }
}
</script>

<style scoped>
.header {
  height: 60px;
  background-color: #409EFF;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  color: white;
}

.title {
  font-size: 22px;
  font-weight: bold;
}

.user-info {
  font-size: 14px;
}
</style>