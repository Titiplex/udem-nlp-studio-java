Each lexicon is a name associated with one or more values.
These values can be either literals or paths to files resolved relatively to the YAML.

## Example

````yaml
lexicons:
  spanish_verbs:
    - lexicons/spanish_verbs.csv
  function_words:
    - "to"
    - "from"
````