# iOS API-first reconciliation

Anteater entries now form the base of each meal. UCI adds only missing station placements. Matching uses explicit IDs and UCI-provided aliases, including the API station suffix. Known API variants remain their own dishes; their nutrition is not copied onto a generic configurable parent. API-only entries remain present even when omitted by UCI. Different stations stay distinct. No name-based nutrition inference is performed.

Regression tests first failed for API-only preservation. Updated tests also verify that the captured popcorn-chicken configurable entry resolves to the supplied API variant with its original nutrition. After implementation, ./ios/scripts/test-core.sh passed 31 executed tests; 2 live-network tests were skipped. Xcode ZotMacros simulator build succeeded. No new live-menu parity claim is made: the merge now intentionally retains API-only entries. Applies to the iOS app; Android reconciliation is unchanged.

Existing fresh menu cache may remain visible for up to 15 minutes; pull to refresh applies the new merge immediately. Saved diary snapshots are unchanged.
