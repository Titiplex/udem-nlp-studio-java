The correction loader reads a list of top-level `rules`.
Each rule can have an ``id`` or `name`, and can contain either a `rewrite` block, or a `merge` block.
The types of rewrites actually supported today are `delete`, `before/after`, `gloss.before/gloss.after`, `insert`,
`regex_sub`, `split`, and the merge of sequences.