import request from './index'

export function submitComment(data) {
  return request.post('/public/comment', null, { params: data })
}

export function getComments(articleId) {
  return request.get(`/public/comment/${articleId}`)
}

export function likeArticle(id) {
  return request.put(`/site/article/${id}/like`)
}

export function recordPV() {
  return request.post('/site/pv')
}

export function getStats() {
  return request.get('/site/stats')
}
