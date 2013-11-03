# Terasology Pathfinding module

## Systems

### `MinionMoveSystem`

**Components:** `MinionMoveComponent`

**Events:** `MovingFinishedEvent`, `CannotReachEvent`, `ReachedWalkableBlockEvent`

This systems moves a minion to a target position. Its controlled through setting the `targetBlock` property of the
attached `MinionMoveComponent` of an entity.

Whenever this property is set, the minion will start moving to the target block. It even jumps if required.

As soon as the minion is above a new `WalkableBlock`, a `ReachedWalkableBlockEvent` is fired.
`MovingFinishedEvent` and `CannotReachEvent` are fired, when the target position is reached or if it cannot be reached
(anymore).

### `MinionPathSystem`

**Components:** `MinionPathComponent`

**Events:** `MoveToEvent`, `MovingPathFinishedEvent`, `MovingPathAbortedEvent`

Moves a minion along a path to a given target. To start moving a `MoveToEvent` is fired to the entity. The system
will then request a path from current to target position. Once the path is received, the minion is moved through
each of path's position.

If moving to a position results in a `CannotReachEvent`, a new path is requested automatically. If target position
got invalid, a `MovingPathAbortedEvent` is fired.

If target is reached, `MovingPathFinishedEvent` is fired.

### `BlockSelectionSystem`

**Components:** `BlockSelectionComponent`

**Events:** `ApplyBlockSelectionEvent`

Using an item with a `BlockSelectionComponent` starts the selection mode. Another click with the item will stop it.
The box area between first and second click will form the selection, that is fired using a `ApplyBlockSelectionEvent`.

### `PathfinderSystem`

**Events:** `PathReadyEvent`

A system to find paths through the map. Calculations that may take some time are done in background (preprocessing chunks
and finding path).

Once a path for a request is found, a `PathReadyEvent` is sent back to the entity.

If there is a block change, all paths and all nav data associated with the changed chunk is invalidated. If a block
change occurs when there are pending requests, all pending requests are canceled using a `PathReadyEvent` without any paths.

The nav data (`WalkableBlock` and `Floor`) is *only* modified in the background and *never*, when a path is calculated.

Whenever you operated with nav data, keep in mind that the current instance of a `WalkableBlock` may not be up to date.
Always use `PathfinderSystem.getBlock()` to get the latest block.