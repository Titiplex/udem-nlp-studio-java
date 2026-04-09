This rule inserts a string into a language segment.
The indices ``token`` and `position` are **1-based** in the YAML.
The engine bounds them if needed.

## Grammar

````yaml
rewrite:
  insert:
    segment: <string>
    token: <int>       # index of segment, starting at 1
    position: <int>    # character position, starting at 1
````

## Example

````yaml
- id: insert_h
  targets: i
  rewrite:
    match:
      tokens:
        isword: "in"
    insert:
      segment: "h"
      token: 1
      position: 1
````