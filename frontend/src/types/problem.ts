export type FieldError = {
  field: string
  message: string
}

export type ProblemDetailBody = {
  type?: string
  title: string
  status: number
  detail?: string
  instance?: string
  errors?: FieldError[]
}
