<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { streamSseGet } from '@/utils/sse'
import ManusAssistantBubble from './ManusAssistantBubble.vue'

interface Msg {
  role: 'user' | 'assistant'
  content: string
}

const router = useRouter()
const input = ref('')
const messages = ref<Msg[]>([])
const sending = ref(false)
const scrollRef = ref<HTMLElement | null>(null)
let abort: AbortController | null = null

/** 仅拼接到发往 Manus 接口的 query，不出现在用户气泡中，引导模型用中文成稿 */
const MANUS_MESSAGE_LOCALE_HINT =
  '\n\n（请用简体中文撰写全部面向用户的可见内容：含「整理结果」标题与正文、景点说明、贴士等；必要处可对专有名词保留原文并用括号标注。若有可公开展示的配图，请在成稿中使用 Markdown 图片语法：![景点名或说明](https://公开图片地址)，或单独一行放置以 .jpg/.png/.webp 等结尾的 https 图片直链。）'

async function scrollBottom() {
  await nextTick()
  const el = scrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return
  messages.value.push({ role: 'user', content: text })
  messages.value.push({ role: 'assistant', content: '' })
  input.value = ''
  await scrollBottom()

  sending.value = true
  abort?.abort()
  abort = new AbortController()
  const assistantIndex = messages.value.length - 1

  const payloadForApi = `${text}${MANUS_MESSAGE_LOCALE_HINT}`
  const q = `/ai/manus/chat?message=${encodeURIComponent(payloadForApi)}`

  try {
    await streamSseGet(
      q,
      (chunk) => {
        const cur = messages.value[assistantIndex]
        if (cur) cur.content += chunk
      },
      abort.signal,
    )
  } catch (e: unknown) {
    const err = e as Error
    if (err.name !== 'AbortError') {
      ElMessage.error(err.message || '对话失败')
      messages.value[assistantIndex].content ||= '（响应出错）'
    }
  } finally {
    sending.value = false
    await scrollBottom()
  }
}

function stop() {
  abort?.abort()
  sending.value = false
}
</script>

<template>
  <div class="chat-page">
    <header class="top">
      <el-page-header @back="router.push('/agents')">
        <template #content>
          <span class="title">AngjiaxManus 超级智能体</span>
        </template>
        <template #extra>
          <el-button v-if="sending" size="small" @click="stop">停止</el-button>
        </template>
      </el-page-header>
    </header>

    <div ref="scrollRef" class="messages">
      <div v-if="!messages.length" class="empty">
        与 AngjiaxManus 对话；整理结果默认以简体中文展示，工具细节在「执行过程」中折叠。
      </div>
      <div
        v-for="(m, i) in messages"
        :key="i"
        class="row"
        :class="m.role === 'user' ? 'user' : 'assistant'"
      >
        <div class="bubble">
          <ManusAssistantBubble v-if="m.role === 'assistant'" :content="m.content" />
          <template v-else>{{ m.content }}</template>
        </div>
      </div>
    </div>

    <footer class="composer">
      <el-input
        v-model="input"
        type="textarea"
        :rows="2"
        placeholder="请输入消息..."
        :disabled="sending"
        @keydown.enter.exact.prevent="send"
      />
      <el-button type="primary" :loading="sending" @click="send">发送</el-button>
    </footer>
  </div>
</template>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f2f5;
}

.top {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.title {
  font-weight: 600;
}

.messages {
  flex: 1;
  overflow: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty {
  text-align: center;
  color: #909399;
  margin-top: 48px;
  font-size: 14px;
}

.row {
  display: flex;
}

.row.user {
  justify-content: flex-end;
}

.row.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 78%;
  padding: 10px 14px;
  border-radius: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.5;
}

.row.user .bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.row.assistant .bubble {
  background: #e8eaed;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.composer {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding: 12px 16px 16px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.composer .el-button {
  flex-shrink: 0;
}
</style>
