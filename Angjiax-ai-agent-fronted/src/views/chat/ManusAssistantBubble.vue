<script setup lang="ts">
import { computed } from 'vue'
import { parseManusAssistantContent } from '@/utils/manusMessageFormat'
import { parseAnswerRichSegments } from '@/utils/manusRichSegments'

const props = defineProps<{ content: string }>()

const parsed = computed(() => parseManusAssistantContent(props.content))

const answerSegments = computed(() =>
  parsed.value.displayBody ? parseAnswerRichSegments(parsed.value.displayBody) : [],
)

const plainSegments = computed(() =>
  parsed.value.isToolTrace ? [] : parseAnswerRichSegments(props.content),
)

const collapseTitle = computed(() => {
  const n = parsed.value.steps.length
  return n ? `执行过程（${n} 步）` : '执行过程'
})

function onImgError(ev: Event) {
  const el = ev.target as HTMLImageElement | null
  if (el) el.style.display = 'none'
}
</script>

<template>
  <div v-if="parsed.isToolTrace && parsed.steps.length" class="manus-wrap">
    <section v-if="parsed.displayBody" class="answer-card">
      <div class="answer-label">{{ parsed.readFileBody ? '整理结果' : '智能回答' }}</div>
      <div class="answer-body-mixed">
        <template v-for="seg in answerSegments" :key="`a-${seg.type}-${seg.text || seg.src}`">
          <span v-if="seg.type === 'text'" class="answer-text">{{ seg.text }}</span>
          <figure v-else class="answer-fig">
            <img
              class="answer-img"
              :src="seg.src"
              :alt="seg.alt"
              loading="lazy"
              referrerpolicy="no-referrer"
              decoding="async"
              @error="onImgError"
            />
            <figcaption v-if="seg.alt && seg.alt !== '插图'" class="answer-cap">{{ seg.alt }}</figcaption>
          </figure>
        </template>
      </div>
    </section>
    <section v-else class="hint-card">
      暂未能从工具输出中解析出可读正文（例如各步「结果:」后的引号未闭合、或仅有文件路径）。请展开下方「执行过程」查看各步摘要；完整流在「原始助手文本」中。
    </section>

    <el-collapse class="proc-collapse" accordion>
      <el-collapse-item :title="collapseTitle" name="proc">
        <div
          v-for="s in parsed.steps"
          :key="`${s.step}-${s.tool}`"
          class="step-block"
        >
          <div class="step-meta">
            <span class="step-no">第 {{ s.step }} 步</span>
            <span class="step-tool">{{ s.tool }}</span>
          </div>
          <div class="step-summary">{{ s.summary }}</div>
          <div v-if="s.detailLines.length" class="step-details">
            <p v-for="(line, i) in s.detailLines" :key="i" class="detail-line">{{ line }}</p>
          </div>
        </div>
      </el-collapse-item>
      <el-collapse-item title="原始助手文本（调试）" name="raw">
        <pre class="raw-dump">{{ content }}</pre>
      </el-collapse-item>
    </el-collapse>
  </div>
  <div v-else class="plain plain-rich">
    <template v-for="seg in plainSegments" :key="`p-${seg.type}-${seg.text || seg.src}`">
      <span v-if="seg.type === 'text'" class="plain-text">{{ seg.text }}</span>
      <figure v-else class="answer-fig">
        <img
          class="answer-img"
          :src="seg.src"
          :alt="seg.alt"
          loading="lazy"
          referrerpolicy="no-referrer"
          decoding="async"
          @error="onImgError"
        />
        <figcaption v-if="seg.alt && seg.alt !== '插图'" class="answer-cap">{{ seg.alt }}</figcaption>
      </figure>
    </template>
  </div>
</template>

<style scoped>
.manus-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.answer-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px 14px;
}

.answer-label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
  letter-spacing: 0.02em;
}

.answer-body-mixed {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
  line-height: 1.55;
  color: #1e293b;
}

.answer-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.answer-fig {
  margin: 0;
}

.answer-img {
  display: block;
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.answer-cap {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

.hint-card {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  padding: 8px 0;
}

.proc-collapse {
  border: none;
  --el-collapse-header-bg-color: transparent;
}

.proc-collapse :deep(.el-collapse-item__header) {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  padding-left: 0;
}

.proc-collapse :deep(.el-collapse-item__wrap) {
  border: none;
}

.proc-collapse :deep(.el-collapse-item__content) {
  padding-bottom: 8px;
}

.step-block {
  padding: 10px 0;
  border-bottom: 1px solid #eef2f7;
}

.step-block:last-child {
  border-bottom: none;
}

.step-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.step-no {
  font-size: 12px;
  font-weight: 700;
  color: #94a3b8;
}

.step-tool {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.step-summary {
  font-size: 13px;
  color: #334155;
  line-height: 1.45;
}

.step-details {
  margin-top: 8px;
  padding: 8px 10px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8ecf1;
}

.detail-line {
  margin: 0 0 6px;
  font-size: 12px;
  line-height: 1.45;
  color: #475569;
  word-break: break-word;
}

.detail-line:last-child {
  margin-bottom: 0;
}

.raw-dump {
  margin: 0;
  max-height: 240px;
  overflow: auto;
  font-size: 11px;
  line-height: 1.35;
  color: #64748b;
  white-space: pre-wrap;
  word-break: break-word;
}

.plain {
  font-size: 14px;
  line-height: 1.5;
}

.plain-rich {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plain-text {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
