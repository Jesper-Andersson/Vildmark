# Shader pack compatibility

Shader packs decide which blocks wave from their own `shaders/block.properties`, which maps
registry IDs to numeric `mc_Entity` IDs. Nordic blocks aren't listed, so
`nordic:lingonberry_shrub`, `nordic:blueberry_shrub` and `nordic:pine_leaves` don't wave.
The mod cannot add IDs to a pack; patch the pack instead.

1. Copy the pack (zip or folder) in `shaderpacks/` under a new name -- don't edit the original download.
2. Open `shaders/block.properties` in the copy.
3. Append the IDs from the matching snippet to the existing lines for vanilla leaves / sweet_berry_bush:
   - `complementary.properties.txt` (Complementary Reimagined r5.9.3, verified)
   - `bsl.properties.txt` (BSL-style, look up the entry numbers yourself)
4. Select the copy in Iris and reload shaders (`R`).

Check: pine leaves and both shrubs sway, vanilla leaves still sway, nothing else changed.
