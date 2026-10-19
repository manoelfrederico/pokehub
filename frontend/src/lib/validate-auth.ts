import type { FieldError } from '../types/problem'

const USERNAME_PATTERN = /^[A-Za-z0-9._-]+$/

export function validateAuthForm(username: string, password: string): FieldError[] {
  const errors: FieldError[] = []
  const u = username.trim()
  if (u.length < 3 || u.length > 30) {
    errors.push({ field: 'username', message: 'must be 3–30 characters' })
  } else if (!USERNAME_PATTERN.test(u)) {
    errors.push({
      field: 'username',
      message: 'may only contain letters, digits, . _ -',
    })
  }
  if (password.length < 8 || password.length > 72) {
    errors.push({ field: 'password', message: 'must be 8–72 characters' })
  } else if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) {
    errors.push({
      field: 'password',
      message: 'must contain at least one letter and one digit',
    })
  }
  return errors
}
