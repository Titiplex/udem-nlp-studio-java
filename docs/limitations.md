# Limitations and implicit behaviors to be aware of

### 1. ``merge``

``merge`` only currently merges sequences of length 2.
The loader accepts lists of sequences, but the actual implementation only merges sequences of two adjacent tokens.

### 2. ``targets``

``targets`` only truly handles `i` and `j`.
Any other value falls back as `i`.

### 3. ``between.length``

``between.length`` is a fixed distance.
It isn't "between 0 and N", it is exactly the distance used to calculate the partner sequence.

### 4. ``extractors``, ``role``, ``value_if_present`` and ``value_if_absent``

``extractors``, ``role``, ``value_if_present`` and ``value_if_absent`` are mostly descriptive.
The actual regex is constructed from ``series`` + `values.person` + `values.number.suffix`.
The other fields don't directly drive the current regex extraction.

### 5. ``into``

``into`` is well supported for annotation.
The context is stored under `into ` if provided, otherwise under the name of the extractor.
So ``{ab.A.person}`` is valid if `into: ab` is given.