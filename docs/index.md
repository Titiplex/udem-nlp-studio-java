# NLP Studio YAML Reference

This documentation describes the YAML grammars currently supported by the NLP Studio engine.

It focuses on the **actual runtime behavior** of the current implementation, including some implementation-specific
constraints and limitations.

## Two YAML families

The project currently relies on two distinct YAML configurations:

- **Correction YAML**: used to normalize, rewrite, split, merge, delete, or otherwise transform aligned tokens before
  annotation.
- **Annotation YAML**: used to define POS/features, gloss mappings, lexicons, extractors, and rule-based annotation
  behavior for CoNLL-U generation.

## Recommended reading order

If you are new to the project, the best reading order is:

1. **Correction Rules → Overview**
2. **Annotation Rules → Overview**
3. **Limitations**

Then you can move to the detailed tag/zone pages depending on what you want to edit.

## Important note

This site documents the **current engine behavior**, not an idealized or planned grammar.

Some parts of the YAML surface look more generic than they really are at runtime. For example:

- some fields are currently more descriptive than operational,
- some syntax variants exist for backward compatibility,
- some operations are supported only in a narrower form than the YAML may suggest.

When in doubt, prefer the documented behavior here and the test YAML files in the repository.

## Main concepts

A few recurring concepts appear throughout the documentation:

- **token**: a full aligned token
- **morpheme**: an individual segment within a token
- **Surface**: the surface form on the language side
- **gloss surface**: the joined gloss side
- **segments**: parts separated by `-`
- **lexicon**: a named set of literals loaded from YAML or files
- **extractor context**: structured values injected by extractors and later reused in templates or conditions

## Scope of the documentation

This reference currently documents:

- the correction rule grammar loaded by the correction YAML loader,
- the annotation YAML zones loaded by the annotation config loader,
- the main supported tags and their semantics,
- the known implementation limits that affect YAML authoring.