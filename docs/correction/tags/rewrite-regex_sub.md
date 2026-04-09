The loader supports ``pattern``, ``repl``, ``scope``, and ``ignore_case``.
``scope`` can be `language`, `gloss`, or both.
For gloss, there is even a dedicated ``GlossReplaceRule`` ; for other cases, the substitution is done on the joined
surface.

## Grammar

````yaml
rewrite:
  regex_sub:
    scope: chuj | gloss | both
    pattern: <regex>
    repl: <string>
    ignore_case: <bool>
````

## Example

````yaml
- id: fix_hi
  rewrite:
    regex_sub:
      scope: chuj
      pattern: "^i+[,.!?;:]?$"
      repl: "hi"
      ignore_case: false
````