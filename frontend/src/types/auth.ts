export type AuthRequest = {
  username: string
  password: string
}

export type UserResponse = {
  id: string
  username: string
}

export type TokenResponse = {
  accessToken: string
  tokenType: string
  expiresIn: number
}
