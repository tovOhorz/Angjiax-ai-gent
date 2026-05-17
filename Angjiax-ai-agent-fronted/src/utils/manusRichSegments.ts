/**
 * 将回答文本解析为富文本片段（文本 + 图片）
 * 支持 Markdown 格式的图片语法: ![alt](src)
 * 支持 HTML 图片标签: <img src="..." alt="...">
 */

export interface RichSegment {
  type: 'text' | 'image'
  text?: string
  src?: string
  alt?: string
}

/**
 * 解析回答中的富文本片段
 * @param content - 原始文本内容，可能包含 Markdown 图片或 HTML 图片标签
 * @returns 富文本片段数组
 */
export function parseAnswerRichSegments(content: string): RichSegment[] {
  if (!content || typeof content !== 'string') {
    return []
  }

  const segments: RichSegment[] = []

  // 首先将 HTML 图片标签转换为 Markdown 格式
  let processedContent = content.replace(
    /<img[^>]*src=["']([^"']+)["'][^>]*(?:alt=["']([^"']*)["'])?[^>]*>/gi,
    (_match, src, alt) => {
      return `![${alt || '插图'}](${src})`
    }
  )

  // 匹配 Markdown 图片语法: ![alt](src)
  const imageRegex = /!\[([^\]]*)\]\(([^)]+)\)/g

  let lastIndex = 0
  let match: RegExpExecArray | null

  while ((match = imageRegex.exec(processedContent)) !== null) {
    // 添加图片前的纯文本
    if (match.index > lastIndex) {
      const textBefore = processedContent.slice(lastIndex, match.index)
      if (textBefore.trim()) {
        segments.push({
          type: 'text',
          text: textBefore,
        })
      }
    }

    // 添加图片片段
    const alt = match[1] || '插图'
    const src = match[2]

    segments.push({
      type: 'image',
      src,
      alt,
    })

    lastIndex = match.index + match[0].length
  }

  // 添加剩余的文本
  if (lastIndex < processedContent.length) {
    const remainingText = processedContent.slice(lastIndex)
    if (remainingText.trim()) {
      segments.push({
        type: 'text',
        text: remainingText,
      })
    }
  }

  // 如果没有匹配到任何图片，返回纯文本
  if (segments.length === 0 && processedContent.trim()) {
    segments.push({
      type: 'text',
      text: processedContent,
    })
  }

  return segments
}
