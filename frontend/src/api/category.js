import request from './index'

export function getCategories() {
  return request.get('/category')
}

export function saveCategory(data) {
  return request.post('/category', data)
}

export function updateCategory(data) {
  return request.put('/category', data)
}

export function deleteCategory(id) {
  return request.delete(`/category/${id}`)
}
