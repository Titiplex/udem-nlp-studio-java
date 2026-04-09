Annotation rules are loaded with ``name``, ``scope``, ``priority``, ``match`` and ``set``.
Supported ``scope`` are `token` and `morpheme`.
Matching handles regex, lists, lexicons, extractors, ``require`` and `forbid`.

## Grammar

````yaml
rules:
  - name: <string>
    scope: token | morpheme
    priority: <int>

    match:
      gloss: ...
      in_list: <string | [string, ...]>       # compatible with older format
      in_lexicon: <lexicon-name>
      regex: <regex>
      require: <string | [string, ...]>
      forbid: <string | [string, ...]>
      extract:
        - type: scan_agreement
          extractor: <extractor-name>

    set:
      upos: <UPOS>
      feats:
        <FeatName>: <FeatValue>
      feats_template:
        <FeatName>: <template>
      extract:
        - type: scan_agreement
          extractor: <extractor-name>
          into: <context-key>
````

## Semantic

- ``match.gloss`` can be a simple string or a map.
- ``in_list`` is supported for compatibility.
- ``in_lexicon`` references a loaded lexicon at the top-level.
- ``regex`` constructs a `Pattern`.
- ``require`` and ``forbid`` test context paths.
- ``match.extract`` and ``set.extract`` are used to launch extractors.

## Examples

### Minimal Example

````yaml
- name: identify verbs from gloss lexicon
  scope: morpheme
  match:
    gloss:
      in_lexicon: spanish_verbs
  set:
    upos: VERB
````

### Example with templates

````yaml
- name: scan agreement on verb tokens
  scope: token
  match:
    gloss:
      in_lexicon: spanish_verbs
  set:
    extract:
      - type: scan_agreement
        extractor: agreement_verbs
        into: ab
    feats_template:
      Pers[subj]: "{ab.A.person}"
      Number[subj]: "{ab.A.number}"
      Pers[obj]: "{ab.B.person}"
      Number[obj]: "{ab.B.number}"
````

This structure exists as it is in the YAML of the test.