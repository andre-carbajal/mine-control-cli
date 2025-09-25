# Changelog

---

## Changes

- Server creation arguments' order was modified to improve clarity and usability.

Before:
`server create (serverName, loaderType, loaderVersion, minecraftVersion)`

After:
`server create (serverName, minecraftVersion, loaderType, loaderVersion)`

## Improvements

- Server creation was refactored to improve maintainability and readability. This change does not affect the
  functionality of the server creation process but enhances the code structure for future development.
- Forge version order is now displayed correctly.