import request from './index'

export function getTags() {
  return request.get('/tag')
}

export function saveTag(data) {
  return request.post('/tag', data)
}

export function updateTag(data) {
  return request.put('/tag', data)
}

export function deleteTag(id) {
  return request.delete(`/tag/${id}`)
}
