import type { StatResponse } from '../../types/pokemon'

const MAX_STAT = 255

type StatsTableProps = {
  stats: StatResponse[]
}

export function StatsTable({ stats }: StatsTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-left text-sm">
        <caption className="sr-only">Base stats</caption>
        <thead>
          <tr className="border-b border-slate-200 text-slate-600">
            <th scope="col" className="py-2 pr-4 font-medium">
              Stat
            </th>
            <th scope="col" className="py-2 pr-4 font-medium">
              Value
            </th>
            <th scope="col" className="py-2 font-medium">
              Bar
            </th>
          </tr>
        </thead>
        <tbody>
          {stats.map((stat) => {
            const pct = Math.min(100, Math.round((stat.baseValue / MAX_STAT) * 100))
            return (
              <tr key={stat.name} className="border-b border-slate-100">
                <th scope="row" className="py-2 pr-4 font-medium capitalize text-slate-900">
                  {stat.name}
                </th>
                <td className="py-2 pr-4 tabular-nums text-slate-800">{stat.baseValue}</td>
                <td className="py-2">
                  <div
                    className="h-2 w-full max-w-xs rounded-full bg-slate-200"
                    role="meter"
                    aria-valuemin={0}
                    aria-valuemax={MAX_STAT}
                    aria-valuenow={stat.baseValue}
                    aria-label={`${stat.name} ${stat.baseValue}`}
                  >
                    <div
                      className="h-2 rounded-full bg-indigo-600"
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
