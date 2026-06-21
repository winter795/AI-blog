import request from './index'

export function getArticlePage(params) {
  return request.get('/article/page', { params })
}

export function getArticle(id) {
  return request.get(`/article/${id}`)
}

export function saveArticle(data) {
  return request.post('/article', data, { params: { tagIds: data.tagIds?.join(',') } })
}

export function updateArticle(data) {
  return request.put('/article', data, { params: { tagIds: data.tagIds?.join(',') } })
}

export function deleteArticle(id) {
  return request.delete(`/article/${id}`)
}

export function toggleArticleStatus(id) {
  return request.put(`/article/${id}/toggle`)
}
