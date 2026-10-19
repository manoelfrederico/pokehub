import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { renderWithProviders } from '../../test/test-utils'
import { EvolutionTree } from './EvolutionTree'

describe('EvolutionTree', () => {
  it('renders branching evolution links', () => {
    renderWithProviders(
      <EvolutionTree
        currentId={1}
        node={{
          id: 1,
          name: 'bulbasaur',
          sprite: null,
          evolvesTo: [
            {
              id: 2,
              name: 'ivysaur',
              sprite: null,
              evolvesTo: [
                {
                  id: 3,
                  name: 'venusaur',
                  sprite: null,
                  evolvesTo: [],
                },
              ],
            },
          ],
        }}
      />,
    )

    expect(screen.getByText(/bulbasaur/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ivysaur/i })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /venusaur/i })).toBeInTheDocument()
  })
})
