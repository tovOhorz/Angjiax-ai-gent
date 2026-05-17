<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ChatDotRound, Cpu, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const cards = [
  {
    key: 'rag',
    title: 'RAG 智能问答助手',
    badge: '企业级检索增强',
    desc: '对接知识库与向量检索，长上下文理解与可控引用，让回答可追溯、可审计，适合文档密集场景。',
    foot: '流式输出 · 低幻觉 · 秒级定位原文',
    path: '/rag-chat',
    icon: ChatDotRound,
    gradient: 'linear-gradient(135deg, #1d4ed8 0%, #6366f1 45%, #a855f7 100%)',
  },
  {
    key: 'manus',
    title: 'AngjiaxManus 超级智能体',
    badge: '多步推理与工具编排',
    desc: '复杂任务自动拆解、并行调用与自我校验，像一位不知疲倦的高级助理，把「想法」推进到「结果」。',
    foot: '规划 · 执行 · 反思闭环',
    path: '/manus-chat',
    icon: Cpu,
    gradient: 'linear-gradient(135deg, #047857 0%, #059669 40%, #14b8a6 100%)',
  },
]
</script>

<template>
  <div class="hub">
    <header class="hero">
      <p class="eyebrow">Angjiax · AI 能力矩阵</p>
      <h1>选一个助手，开始高强度产出</h1>
      <p class="lead">
        左侧是「精准可控的知识问答」，右侧是「敢接复杂活的超级智能体」。同一套账号体系，登录即用。
      </p>
    </header>

    <div class="cards">
      <article
        v-for="c in cards"
        :key="c.key"
        class="card"
        role="button"
        tabindex="0"
        @click="router.push(c.path)"
        @keydown.enter="router.push(c.path)"
      >
        <div class="card-bg" :style="{ background: c.gradient }" />
        <div class="card-inner">
          <div class="top">
            <div class="icon-wrap">
              <el-icon :size="26"><component :is="c.icon" /></el-icon>
            </div>
            <span class="badge">{{ c.badge }}</span>
          </div>
          <h2>{{ c.title }}</h2>
          <p class="desc">{{ c.desc }}</p>
          <div class="foot">
            <span>{{ c.foot }}</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.hub {
  padding: 28px 24px 48px;
  max-width: 1100px;
  margin: 0 auto;
}

.hero {
  margin-bottom: 28px;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #6366f1;
}

.hero h1 {
  margin: 0 0 10px;
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.lead {
  margin: 0;
  max-width: 720px;
  font-size: 15px;
  line-height: 1.65;
  color: #475569;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.card {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  min-height: 260px;
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.18);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 22px 56px rgba(15, 23, 42, 0.22);
}

.card-bg {
  position: absolute;
  inset: 0;
  opacity: 0.96;
}

.card-inner {
  position: relative;
  z-index: 1;
  height: 100%;
  padding: 22px 22px 20px;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(6px);
}

.card h2 {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.3;
}

.desc {
  margin: 0;
  flex: 1;
  font-size: 14px;
  line-height: 1.65;
  opacity: 0.95;
}

.foot {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 600;
  opacity: 0.9;
}

.arrow {
  font-size: 18px;
}
</style>
