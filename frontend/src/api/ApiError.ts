import type { FieldError, ProblemDetailBody } from '../types/problem'

export class ApiError extends Error {
  readonly status: number
  readonly title: string
  readonly detail: string | undefined
  readonly errors: FieldError[]

  constructor(problem: ProblemDetailBody) {
    const message = problem.detail ?? problem.title
    super(message)
    this.name = 'ApiError'
    this.status = problem.status
    this.title = problem.title
    this.detail = problem.detail
    this.errors = problem.errors ?? []
  }
}

export class NetworkError extends Error {
  constructor(cause?: unknown) {
    super('Network request failed')
    this.name = 'NetworkError'
    this.cause = cause
  }
}
