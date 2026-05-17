/**
 * 将 Manus 超级智能体流式拼接的「Step N: 工具 xxx 完成了它的任务！结果: "…"」
 * 解析为结构化步骤，便于在聊天界面友好展示（不依赖后端改格式）。
 */

export interface ManusParsedStep {
  step: number
  tool: string
  /** 原始步骤全文（不含 Step 前缀） */
  raw: string
  /** 从 结果: "…" 中解包出的内容；流未完成时可能为 null */
  resultText: string | null
  /** 一行摘要，用于折叠列表 */
  summary: string
  /** 可选的展开区展示（如搜索结果的链接列表） */
  detailLines: string[]
}

export interface ManusParsedMessage {
  /** 是否像 Manus 工具链轨迹 */
  isToolTrace: boolean
  steps: ManusParsedStep[]
  /** readFile 解包出的正文（若有） */
  readFileBody: string | null
  /**
   * 气泡主展示区正文：优先 readFile；否则从 doTerminate、网页抓取、检索结果等推导，
   * 避免智能体未执行 readFile 时用户只能看到「未检测到成稿」。
   */
  displayBody: string | null
}

const TOOL_DONE = /工具\s+(\w+)\s+完成了它的任务！/

/** 从「结果: "」起按转义规则读到结束双引号，返回内部字符串；不完整返回 null */
function extractQuotedPayload(segment: string): string | null {
  const markers = ['结果: "', '结果："']
  let start = -1
  let markerLen = 0
  for (const m of markers) {
    const idx = segment.indexOf(m)
    if (idx >= 0 && (start < 0 || idx < start)) {
      start = idx
      markerLen = m.length
    }
  }
  if (start < 0) return null
  let i = start + markerLen
  let out = ''
  while (i < segment.length) {
    const c = segment[i]
    if (c === '\\' && i + 1 < segment.length) {
      out += segment[i + 1]
      i += 2
      continue
    }
    if (c === '"') return out
    out += c
    i++
  }
  return null
}

function stripHtmlToText(html: string, maxLen = 500): string {
  const text = html
    .replace(/<script[\s\S]*?<\/script>/gi, ' ')
    .replace(/<style[\s\S]*?<\/style>/gi, ' ')
    .replace(/<[^>]+>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  return text.length > maxLen ? `${text.slice(0, maxLen)}…` : text
}

function isSafeHttpUrl(href: string): boolean {
  try {
    const u = new URL(href.trim())
    return u.protocol === 'http:' || u.protocol === 'https:'
  } catch {
    return false
  }
}

/** 从 HTML 中提取可外链的图片，转成 Markdown 片段供前端渲染 */
function extractHtmlImageMarkdown(html: string): string {
  const re = /<img\b[^>]*\bsrc\s*=\s*["']([^"']+)["'][^>]*>/gi
  const urls: string[] = []
  let m: RegExpExecArray | null
  while ((m = re.exec(html)) !== null) {
    const href = m[1].trim().replace(/&amp;/g, '&')
    if (isSafeHttpUrl(href)) urls.push(href)
  }
  return [...new Set(urls)].map((u) => `![](${u})`).join('\n')
}

function normalizeDoTerminatePayload(raw: string): string {
  const t = raw.trim()
  if (!t) return t
  if (t === '{}' || t === '[]' || t === 'null') return ''
  if (t.startsWith('{') || t.startsWith('[')) {
    try {
      const o = JSON.parse(t) as unknown
      if (o && typeof o === 'object' && !Array.isArray(o)) {
        const rec = o as Record<string, unknown>
        for (const k of ['message', 'answer', 'content', 'summary', 'result', 'output', 'text']) {
          const v = rec[k]
          if (typeof v === 'string' && v.trim()) return v.trim()
        }
        return ''
      }
    } catch {
      /* 非 JSON 则原样返回 */
    }
  }
  return t
}

function basename(p: string): string {
  const s = p.replace(/\\/g, '/')
  const idx = s.lastIndexOf('/')
  return idx >= 0 ? s.slice(idx + 1) : s
}

function parseSearchResultItems(jsonStr: string): unknown[] {
  const t = jsonStr.trim()
  try {
    const data = JSON.parse(t) as unknown
    return Array.isArray(data) ? data : [data]
  } catch {
    try {
      const wrapped = JSON.parse(`[${t}]`) as unknown
      return Array.isArray(wrapped) ? wrapped : [wrapped]
    } catch {
      return []
    }
  }
}

function summarizeSearchWeb(jsonStr: string): { summary: string; lines: string[] } {
  try {
    const items = parseSearchResultItems(jsonStr)
    const lines: string[] = []
    for (const it of items) {
      if (it && typeof it === 'object' && ('title' in it || 'link' in it)) {
        const o = it as { title?: string; link?: string; snippet?: string }
        const t = (o.title || o.link || '无标题').slice(0, 120)
        const sn = (o.snippet || '').replace(/\s+/g, ' ').slice(0, 160)
        lines.push(`· ${t}`)
        if (o.link) lines.push(`  ${o.link}`)
        if (sn) lines.push(`  ${sn}${(o.snippet?.length || 0) > 160 ? '…' : ''}`)
      }
    }
    const summary =
      lines.length > 0
        ? `检索到 ${lines.filter((l) => l.startsWith('·')).length} 条相关结果`
        : '网页检索已完成'
    return { summary, lines: lines.slice(0, 15) }
  } catch {
    return { summary: '网页检索已完成（结果解析中或格式异常）', lines: [] }
  }
}

function looksLikeFilePathOnly(s: string): boolean {
  const t = s.trim()
  return t.length < 260 && /^[\w./\\:-]+\.(md|txt|html|json|pdf|png|jpe?g|webp)$/i.test(t)
}

function deriveDisplayBody(
  steps: ManusParsedStep[],
  raw: string,
  firstStepIndex: number | null,
): string | null {
  for (let i = steps.length - 1; i >= 0; i--) {
    if (steps[i].tool === 'readFile' && steps[i].resultText?.trim()) {
      return steps[i].resultText!.trim()
    }
  }

  let preamble: string | null = null
  if (firstStepIndex != null && firstStepIndex > 0) {
    const p = raw.slice(0, firstStepIndex).trim()
    if (p.length >= 15) preamble = p
  }

  let body: string | null = null

  for (let i = steps.length - 1; i >= 0; i--) {
    const st = steps[i]
    if (st.tool === 'doTerminate' && st.resultText?.trim()) {
      const n = normalizeDoTerminatePayload(st.resultText!)
      if (n.length > 0) {
        body = n
        break
      }
    }
  }

  if (!body) {
    for (let i = steps.length - 1; i >= 0; i--) {
      const st = steps[i]
      if (st.tool !== 'scrapeWebPage' || !st.resultText?.trim()) continue
      const t = st.resultText.trim()
      if (t.startsWith('<')) {
        const plain = stripHtmlToText(t, 48_000)
        const imgs = extractHtmlImageMarkdown(t)
        const candidate = [plain, imgs].filter(Boolean).join('\n\n').trim()
        if (candidate.replace(/\s/g, '').length > 40) {
          body = candidate
          break
        }
      } else if (t.length > 40) {
        body = t
        break
      }
    }
  }

  if (!body) {
    const searchChunks: string[] = []
    for (const st of steps) {
      if (st.tool !== 'searchWeb' || !st.resultText?.trim()) continue
      const { lines } = summarizeSearchWeb(st.resultText)
      if (lines.length) searchChunks.push(lines.join('\n'))
    }
    if (searchChunks.length) body = searchChunks.join('\n\n———\n\n')
  }

  if (!body) {
    for (let i = steps.length - 1; i >= 0; i--) {
      const rt = steps[i].resultText?.trim()
      if (!rt || looksLikeFilePathOnly(rt)) continue
      const tool = steps[i].tool
      if (tool === 'writeFile' || tool === 'downloadResource') continue
      if (rt.length > 80) {
        body = rt.slice(0, 24_000)
        break
      }
    }
  }

  if (!body) {
    const chunks: string[] = []
    for (const st of steps) {
      const rt = st.resultText?.trim()
      if (!rt || rt.length < 12) continue
      if (looksLikeFilePathOnly(rt)) continue
      if (st.tool === 'writeFile' || st.tool === 'downloadResource') continue
      const piece = rt.length > 5000 ? `${rt.slice(0, 5000)}…` : rt
      chunks.push(piece)
      if (chunks.length >= 8) break
    }
    if (chunks.length) body = chunks.join('\n\n———\n\n')
  }

  const merged = [preamble, body].filter(Boolean).join('\n\n').trim()
  return merged.length > 0 ? merged : null
}

function summarizeTool(tool: string, payload: string | null): { summary: string; lines: string[] } {
  if (payload == null) {
    return { summary: `${tool} 执行中…`, lines: [] }
  }

  switch (tool) {
    case 'searchWeb':
      return summarizeSearchWeb(payload)
    case 'scrapeWebPage': {
      if (payload.trim().startsWith('<')) {
        const preview = stripHtmlToText(payload, 500)
        return {
          summary: `已抓取网页正文（约 ${preview.length} 字摘要）`,
          lines: preview ? [preview] : [],
        }
      }
      return { summary: '已抓取网页', lines: [payload.slice(0, 400)] }
    }
    case 'downloadResource': {
      const m = payload.match(/[:：]\s*(.+)$/)
      const path = m ? m[1].trim() : payload
      return { summary: `已下载资源：${basename(path)}`, lines: [] }
    }
    case 'writeFile': {
      const m = payload.match(/[:：]\s*(.+)$/)
      const path = m ? m[1].trim() : payload
      return { summary: `已写入文件：${basename(path)}`, lines: [] }
    }
    case 'readFile':
      return { summary: '已读取本地成稿', lines: [] }
    case 'generatePDF': {
      const short = payload.replace(/\s+/g, ' ').slice(0, 200)
      return {
        summary: payload.includes('not recognized') || payload.includes('font')
          ? 'PDF 生成遇到问题（字体配置）'
          : 'PDF 生成',
        lines: short ? [short] : [],
      }
    }
    case 'doTerminate': {
      if (payload?.trim()) {
        const show = normalizeDoTerminatePayload(payload)
        const line =
          show.length > 400 ? `${show.slice(0, 400)}…` : show
        return { summary: '任务结束', lines: line ? [line] : [] }
      }
      return { summary: '任务结束', lines: [] }
    }
    default:
      return {
        summary: `${tool} 已完成`,
        lines: payload.length > 300 ? [`${payload.slice(0, 300)}…`] : [payload],
      }
  }
}

/**
 * 按「Step 数字:」切分整段助手文本（流式过程中可反复调用）。
 */
export function parseManusAssistantContent(raw: string): ManusParsedMessage {
  const isToolTrace = raw.includes('完成了它的任务！')
  if (!isToolTrace) {
    return { isToolTrace: false, steps: [], readFileBody: null, displayBody: null }
  }

  const steps: ManusParsedStep[] = []
  const stepHeader = /Step\s+(\d+)\s*:\s*/gi
  const matches: { step: number; index: number; headerLen: number }[] = []
  let m: RegExpExecArray | null
  while ((m = stepHeader.exec(raw)) !== null) {
    matches.push({ step: parseInt(m[1], 10), index: m.index, headerLen: m[0].length })
  }

  for (let k = 0; k < matches.length; k++) {
    const cur = matches[k]
    const next = matches[k + 1]
    const bodyStart = cur.index + cur.headerLen
    const bodyEnd = next ? next.index : raw.length
    const segmentBody = raw.slice(bodyStart, bodyEnd)
    const step = cur.step
    const toolMatch = segmentBody.match(TOOL_DONE)
    const tool = toolMatch ? toolMatch[1] : 'unknown'
    const resultText = extractQuotedPayload(segmentBody)
    const { summary, lines } = summarizeTool(tool, resultText)
    steps.push({
      step,
      tool,
      raw: segmentBody.trim(),
      resultText,
      summary,
      detailLines: lines,
    })
  }

  let readFileBody: string | null = null
  for (let i = steps.length - 1; i >= 0; i--) {
    if (steps[i].tool === 'readFile' && steps[i].resultText) {
      readFileBody = steps[i].resultText
      break
    }
  }

  const firstStepIndex = matches.length > 0 ? matches[0].index : null
  const displayBody = deriveDisplayBody(steps, raw, firstStepIndex)

  return { isToolTrace: true, steps, readFileBody, displayBody }
}
