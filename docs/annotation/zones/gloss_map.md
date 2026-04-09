The gloss mapping allows to transform glosses into UPOS or CoNLL-U features.
For the features, the loader accepts either a pair ``[FeatName, FeatValue]``, or a full map.

## Grammar

````yaml
gloss_map:
  pos:
    - { <gloss>: <UPOS> }

  feats:
    - { <gloss>: [ <FeatName>, <FeatValue> ] }
    - { <gloss>: { <FeatName>: <FeatValue>, <Feat2>: <FeatValue2> } }
````

## Example

````yaml
gloss_map:
  pos:
    - { "PREP": "ADP" }
    - { "DST": "DET" }

  feats:
    - { "PFV": [ "Aspect", "Perf" ] }
    - { "NEG": [ "Polarity", "Neg" ] }
````