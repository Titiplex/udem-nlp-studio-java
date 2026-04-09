````yaml
lexicons:
  <lexicon-name>: <path | literal | [path|literal, ...]>

def:
  pos: [ <UPOS>, ... ]
  feats: [ <FeatName>, ... ]

gloss_map:
  pos:
    - { <gloss>: <UPOS> }
  feats:
    - { <gloss>: [ <FeatName>, <FeatValue> ] }
    # or
    - { <gloss>: { <FeatName>: <FeatValue>, ... } }

extractors:
  <extractor-name>:
    tag_schema: ...
    routing: ...

rules:
  - name: <string>
    scope: token | morpheme
    priority: <int>
    match: ...
    set: ...
````

All of this is explicitly loaded by ``AnnotationConfigLoader``.