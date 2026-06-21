const defaultTitle = '个人AI博客'
const defaultDesc = '基于 Vue 3 + Spring Boot 的个人AI博客，支持 AI 智能搜索与知识问答'

const metaCache = {}

function ensureMeta(name, content, isProperty = false) {
  const attr = isProperty ? 'property' : 'name'
  let el = document.querySelector(`meta[${attr}="${name}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, name)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

export function useSEO(title, desc) {
  const t = title ? `${title} - ${defaultTitle}` : defaultTitle
  const d = desc || defaultDesc
  const key = t + d

  if (metaCache[key]) return
  metaCache[key] = true

  document.title = t
  ensureMeta('description', d)
  ensureMeta('og:title', t, true)
  ensureMeta('og:description', d, true)
  ensureMeta('og:type', 'article', true)
  ensureMeta('twitter:title', t)
  ensureMeta('twitter:description', d)
}
