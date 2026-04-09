It is the classic surface rewriting.
A parallel version exists for glosses via ``rewrite.gloss.before`` / `rewrite.gloss.after`.
If only the ``gloss`` part is provided, the loader creates a rewrite rule for gloss only.

## Grammar

````yaml
rewrite:
  before: <string | [string, ...]>
  after: <string | [string, ...]>

  gloss:
    before: <string | [string, ...]>
    after: <string | [string, ...]>
````

## Semantic

- if ``after`` has a single value, it is applied to all values of ``before``
- if ``after`` has multiple values, the mapping is position-by-position,
- the engine tries to rewrite by segment and to rewrite the full surface joined by ``-``.

## Example

````yaml
- id: normalize_surface
  rewrite:
    match:
      tokens:
        any: [ "ha" ]
    before: [ "ha", "ja" ]
    after: [ "ha'" ]

- id: normalize_gloss
  rewrite:
    match:
      gloss:
        any: [ "A1SG", "A-1SG" ]
    gloss:
      before: [ "A1SG", "A-1SG" ]
      after: [ "A1" ]
````