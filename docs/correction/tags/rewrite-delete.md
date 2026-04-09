The engine supports two variants : suppression of characters (``type: chars``) and deletion of parts/morphemes (
``type: part``).

## Grammar

````yaml
rewrite:
  delete:
    type: chars | part
    chars: <string | [string, ...]>
````

## Semantic

- ``type: chars`` removes the characters in the language and gloss segments.
- ``type: part`` removes the whole segments equal to these values.
- If a token loses all its surface but keeps glosses, the engine tries to move these glosses to the next token.

## Example

````yaml
- id: remove_quotes
  rewrite:
    delete:
      type: chars
      chars: [ '"', "'" ]
````