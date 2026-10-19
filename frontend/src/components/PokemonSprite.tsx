import { useState } from 'react'

type PokemonSpriteProps = {
  src: string | null
  name: string
  className?: string
}

export function PokemonSprite({ src, name, className = '' }: PokemonSpriteProps) {
  const [failed, setFailed] = useState(false)
  const showPlaceholder = !src || failed

  return (
    <div
      className={`relative flex aspect-square w-full items-center justify-center overflow-hidden rounded-md bg-slate-100 ${className}`}
    >
      {showPlaceholder ? (
        <span
          className="text-xs font-medium uppercase tracking-wide text-slate-500"
          aria-hidden="true"
        >
          No image
        </span>
      ) : (
        <img
          src={src}
          alt={`${name} sprite`}
          className="h-full w-full object-contain"
          width={96}
          height={96}
          loading="lazy"
          onError={() => setFailed(true)}
        />
      )}
    </div>
  )
}
