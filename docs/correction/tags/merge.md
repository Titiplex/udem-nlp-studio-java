The merge supports explicit sequences of two adjacent tokens.
The engine then merges surfaces and glosses of the two tokens.
In practice, the current implementation only supports sequences of length 2.

## Grammar
````yaml
merge:
  match:
    tokens:
      - [ <token1>, <token2> ]
      # or
      - <token1>
      - <token2>
````

## Example

````yaml
- id: merge_pronoun_pair
  merge:
    match:
      tokens:
        - [ "ha'", "in" ]
````