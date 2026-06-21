import request from './index'

export function getCommentPage(params) {
  return request.get('/admin/comment/page', { params })
}

export function auditComment(id, status) {
  return request.put(`/admin/comment/${id}/audit`, null, { params: { status } })
}

export function deleteComment(id) {
  return request.delete(`/admin/comment/${id}`)
}
