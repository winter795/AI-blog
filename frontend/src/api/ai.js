import request from './index'

// SSE 流式搜索 - 返回 EventSource 风格的 fetch
export function aiSearch(query, onToken, onSources, onDone, onError) {
  const token = localStorage.getItem('token') || ''
  fetch(`/api/public/ai/search?q=${encodeURIComponent(query)}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(async resp => {
    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('event:sources')) continue // name header
        if (line.startsWith('event:token')) continue
        if (line.startsWith('event:error')) { onError(line.replace('event:error', '').trim()); return }
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          try {
            const json = JSON.parse(data)
            if (Array.isArray(json) && json.length > 0 && json[0].title) {
              onSources(json)
            } else {
              onToken(data)
            }
          } catch { onToken(data) }
        }
      }
    }
    onDone()
  }).catch(onError)
}

export function getSuggestions(q) {
  return request.get('/public/ai/suggest', { params: { q } })
}

export function getRelated(articleId) {
  return request.get(`/public/ai/related/${articleId}`)
}

export function generateSummary(content) {
  return request.post('/admin/ai/summary', { content })
}

export function generateTags(content) {
  return request.post('/admin/ai/tags', { content })
}
