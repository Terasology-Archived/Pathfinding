# Terasology Pathfinding module

## Systems

### `BehaviorSystem`

Manages entity behaviors using a behavior tree.

Currently there are the following behaviors implemented:
* `Sequence` - All children are executed after each other. Failing child will fail the sequence.
* `Selector` - All children are executed after each other. Succeeding child will succeed the selector.
* `Parallel` - All children are executed in parallel. When the parallel finishes, depends on its policies.
* `Monitor` - All children are executed in parallel. As soon as any child succeeds or fails, the monitor succeed or fail.
* `Repeat`   - Repeats its decorated behavior forever.
* `Counter`  - Succeeds after given number of executions.
* `Filter`   - **todo** Selector with conditions and actions
* `PlayAnimation` - Plays a given animation.
* `MoveTo`   - Moves the entity to MinionMoveComponent.target
* `FindPathTo`   - Finds path to MinionPathComponent.target
* `MoveAlongPath`   - Sequence of `MoveTo` along the path stored in MinionPathComponent.path
* `MoveToWalkableBlock`   - Moves the entity to nearby walkable block (navigation graph node)
* `SetTargetToLocalPlayer` - Sets MinionPathComponent.target to local player position

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

Whenever you operate with nav data, keep in mind that the current instance of a `WalkableBlock` may not be up to date.
Always use `PathfinderSystem.getBlock()` to get the latest block.


## Behaviors
