const API_BASE = import.meta.env.VITE_API_BASE || '/api'

function joinBase(path: string): string {
  const base = API_BASE.replace(/\/$/, '')
  const p = path.startsWith('/') ? path : `/${path}`
  return `${base}${p}`
}

/**
 * 读取 Spring SSE（text/event-stream），将每条 data: 内容交给 onChunk
 */
export async function streamSseGet(
  pathWithQuery: string,
  onChunk: (text: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  const url = joinBase(pathWithQuery)
  const res = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'text/event-stream',
    },
    signal,
  })
  if (!res.ok) {
    throw new Error(`SSE 请求失败：${res.status}`)
  }
  const reader = res.body?.getReader()
  if (!reader) {
    throw new Error('无法读取响应流')
  }
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    let idx: number
    buffer = buffer.replace(/\r\n/g, '\n')
    while ((idx = buffer.indexOf('\n')) >= 0) {
      const line = buffer.slice(0, idx).replace(/\r$/, '').trimEnd()
      buffer = buffer.slice(idx + 1)
      if (line.startsWith('data:')) {
        const payload = line.slice(5).trimStart()
        if (payload && payload !== '[DONE]') {
          onChunk(payload)
        }
      }
    }
  }
}
