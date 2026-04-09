An **extractor** is currently defined by a ``tag_schema`` and a list of ``routing`` rules.
The current engine builds a regex from ``tag_schema.series`` and ``tag_schema.values.person/number``, extracts values
like ``A.person``, ``A.number``, and then can inject features via ``routing``.``

## Grammar

````yaml
extractors:
  <name>:
    tag_schema:
      series:
        <SeriesName>:
          role: <string>              # informative for the moment
      values:
        person: [ <string>, ... ]
        number:
          suffix: <string>
          value_if_present: <string>   # informative for the moment
          value_if_absent: <string>    # informative for the moment

    routing:
      - when: <condition-string>
        set:
          <FeatName>: <literal-or-template>
````

## Example

````yaml
extractors:
  agreement_verbs:
    tag_schema:
      series:
        A: { role: "ERG" }
        B: { role: "ABS" }
      values:
        person: [ "1", "2", "3" ]
        number:
          suffix: "PL"
          value_if_present: "Plur"
          value_if_absent: "Sing"
    routing:
      - when: "has(A) & has(B)"
        set: { SubCat: "Trans" }
      - when: "has(B) & !has(A)"
        set: { SubCat: "Intrans" }
````