The parser currently supports the following forms for matching: `match.tokens`, `match.gloss`, `match.surface`,
as well as ``between.length`` and `targets`. The actual fields consumed are in `MatchSpec` and in
`TokenPatternMatcher`.

## Grammar

````yaml
rewrite:
  match:
    tokens:
      - <token1>
      - <token2>
      # or sequence list
      # - [<token1>, <token2>]
      # - [<token3>, <token4>]

      isword: <string | [string, ...]>
      any: <string | [string, ...]>
      startswith: <string | [string, ...]>
      endswith: <string | [string, ...]>
      has_segment: <string | [string, ...]>
      startswith_vowel: <bool>

    gloss:
      <string>
      # or
      any: <string | [string, ...]>
      values: <string | [string, ...]>
      starts_with: <string | [string, ...]>
      in_lexicon: <lexicon-name>

    surface:
      side: i | j
      root_in_lexicon: <lexicon-name>
      root_startswith_vowel: <bool>

  between:
    length: <int>

targets: i | j
````

## Semantic

``tokens`` can match :

- a explicit sequence of tokens,
- a full word via ``isword``
- any segment via `any`
- a prefix via ``startswith``
- a suffix via ``endswith``
- a morphological segment via ``has_segment``
- or the fact that the surface starts with a vowel via ``startswith_vowel``

``gloss`` can match :

- a simple value
- a list ``any`` / `values`
- a gloss prefix via ``starts_with``,
- or a lexicon via ``in_lexicon``.

``between.length`` imposes a fixed distance between the elements of a sequence.
``targets: i|j`` says which element of the sequence becomes the target of the transformation. For j, the matcher
takes the partner at index i + gap + 1.

``surface.side``, `root_in_lexicon` and `root_startswith_vowel` are used to enforce some constraints on the token i or
its
partner j; notably for phonological rules depending on a root.

## Example

````yaml
- id: apostrophe_before_vowel_root
  targets: i
  rewrite:
    match:
      tokens:
        startswith: "ix"
      surface:
        side: j
        root_startswith_vowel: true
      gloss:
        in_lexicon: spanish_verbs
      between:
        length: 0
    before: [ "ix" ]
    after: [ "ix'" ]
````