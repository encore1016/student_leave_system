<template>
  <div class="menu">
    <ul>
      <li @click="goPage('/home')">首页</li>
      <li @click="goPage('/apply')">请假申请</li>
      <li @click="goPage('/record')">请假记录</li>
      <li @click="goPage('/user')">个人中心</li>
    </ul>
  </div>
</template>

<script>
export default {
  name: "Menu",

  methods: {
    goPage(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.menu {
  width: 200px;
  height: 100vh;
  background-color: #2f4050;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

li {
  height: 50px;
  line-height: 50px;
  color: white;
  padding-left: 20px;
  cursor: pointer;
}

li:hover {
  background-color: #409EFF;
}
</style>