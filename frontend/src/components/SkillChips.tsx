import type { SkillResponse } from '../types/pokemon'

type SkillChipsProps = {
  skills: SkillResponse[]
}

export function SkillChips({ skills }: SkillChipsProps) {
  if (skills.length === 0) {
    return <p className="text-sm text-slate-500">No skills listed</p>
  }

  return (
    <ul className="flex flex-wrap gap-2" aria-label="Skills">
      {skills.map((skill) => (
        <li key={skill.name}>
          <span
            className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${
              skill.hidden
                ? 'border border-dashed border-slate-400 bg-slate-100 text-slate-600'
                : 'bg-indigo-100 text-indigo-900'
            }`}
          >
            {skill.name}
            {skill.hidden ? (
              <span className="sr-only"> (hidden ability)</span>
            ) : null}
            {skill.hidden ? (
              <span aria-hidden="true" className="ml-1 text-slate-500">
                (hidden)
              </span>
            ) : null}
          </span>
        </li>
      ))}
    </ul>
  )
}
