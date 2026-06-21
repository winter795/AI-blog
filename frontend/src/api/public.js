import request from './index'

export function getArticlePage(params) {
  return request.get('/public/article/page', { params })
}

export function getArticleDetail(id) {
  return request.get(`/public/article/${id}`)
}

export function getLatestArticles(n = 5) {
  return request.get('/public/article/latest', { params: { n } })
}

export function getHotArticles(n = 5) {
  return request.get('/public/article/hot', { params: { n } })
}

export function getCategories() {
  return request.get('/public/category')
}

export function getTags() {
  return request.get('/public/tag')
}

export function getFriendLinks() {
  return request.get('/public/friend-links')
}
