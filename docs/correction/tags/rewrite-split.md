Two variants exist :

- ``type: suffix`` or `type: end`
- ``type: suffix_with_final_gloss``

## Grammar

````yaml
rewrite:
  split:
    type: suffix | end
    suffixes: <string | [string, ...]>
    # or
    tokens: <string | [string, ...]>
    gloss_placement: left | right | duplicate
````

or

````yaml
rewrite:
  split:
    type: suffix_with_final_gloss
    suffixes: <string | [string, ...]>
    # or
    tokens: <string | [string, ...]>
    gloss_last_match:
      starts_with: <string | [string, ...]>
````

## Semantic

For ``suffix|end``, the token is split in half:

- left part = base
- right part = known suffix

``gloss_placement`` decides where the gloss goes:

- ``right``: gloss goes to the right of the split, left receives `_`
- ``left``: gloss goes to the left of the split, right receives `_`
- ``duplicate``: gloss is duplicated on both sides

For ``suffix_with_final_gloss``, the split is done only if the **last gloss** matches the demanded prefixes, the this
last gloss is moved to the right suffix.

## Example

````yaml
- id: split_suffix
  rewrite:
    match:
      tokens:
        endswith: "on"
    split:
      type: suffix
      suffixes: [ "on" ]
      gloss_placement: right
````