<script setup lang="ts">
function openMail() {
  window.location.href =
    'mailto:3040753112@qq.com?subject=' + encodeURIComponent('Angjiax 需求反馈')
}
</script>

<template>
  <div class="contact">
    <el-card class="card" shadow="never">
      <h1>联系作者</h1>
      <p class="lead">
        有定制需求、功能建议或商业合作，欢迎把想法发到邮箱。我会在方便时回复。
      </p>
      <div class="mail-row">
        <span class="label">邮箱</span>
        <a class="mail" href="mailto:3040753112@qq.com">3040753112@qq.com</a>
      </div>
      <el-button type="primary" @click="openMail">打开邮件客户端</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.contact {
  padding: 20px 24px 48px;
  max-width: 720px;
  margin: 0 auto;
}

.card {
  border-radius: 16px;
  padding: 8px 8px 12px;
}

h1 {
  margin: 0 0 12px;
  font-size: 24px;
  color: #0f172a;
}

.lead {
  margin: 0 0 24px;
  font-size: 15px;
  line-height: 1.7;
  color: #475569;
}

.mail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.label {
  font-weight: 600;
  color: #334155;
}

.mail {
  font-size: 17px;
  font-weight: 700;
  color: #4f46e5;
  text-decoration: none;
}

.mail:hover {
  text-decoration: underline;
}
</style>
