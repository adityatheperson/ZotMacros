# Dining: collect configurable menu entries without inventing serving nutrition

The dining collector only requests `skus.simple`. UCI's October 8, 2026 Anteatery lunch maps 26 additional foods through `skus.configurable`, including Boom Boom Popcorn Chicken at Fire and Ice. Request and collect configurable parents alongside simple products, preserving the date/period mapping and nullable parent nutrition.

New configurable identities include station ID because the current database stores station on the dish; Jasmine Rice appears at both Home and Fire and Ice. Existing simple identities and nutrition parsing remain unchanged. A configurable parent is omitted when one of its variants is already explicitly listed as a simple product at the same station.

Regression checks cover duplicate entries, preservation of existing IDs, both station placements, and simple/parent overlap. The live raw lunch response contains 46 placements, versus 20 simple placements previously collected.

The live comparison also exposed four stale Anteater placements absent from the current raw October 8 map. ZotMacros reconciles membership against UCI; this focused upstream patch adds omitted entry types but does not repair stale relation cleanup.

Limits: full upstream type checks and database ingestion are not yet verified. Configurable parent nutrition is intentionally unknown when only variant nutrition exists. The existing diet-restriction schema cannot express unknown allergen status; this patch does not solve that separate issue. This is a prepared PR draft, not a submitted or merged change.
