# Terasology Behavior Tree

## Behavior nodes

### `Counter` *Decorator*
Starts child a limit number of times.

`SUCCESS`: when child finished with `SUCCESS`n times.
`FAILURE`: as soon as child finishes with `FAILURE`.

### `Inverter` *Decorator*
Inverts the child.

`SUCCESS`: when child finishes `FAILURE`.
`FAILURE`: when child finishes `SUCCESS`.

### `Lookup` *Decorator*
Node that runs a behavior tree.

`SUCCESS`: when tree finishes with `SUCCESS`.
`FAILURE`: when tree finishes with `FAILURE`.

### `Monitor` *Parallel*

`SUCCESS`: as soon as one child node finishes SUCCESS
`FAILURE`: as soon as one child node finishes `FAILURE`.

### `Parallel` *Composite*
All children are evaluated in parallel. Policies for success and failure will define when this node finishes and in which state.

`SUCCESS`: when success policy is fulfilled (one or all children `SUCCESS`).
`FAILURE`, when failure policy is fulfilled (one or all children `FAILURE`).

### `Repeat` *Decorator*
Repeats the child node forever.

`SUCCESS`: Never.
`FAILURE`: as soon as decorated node finishes with `FAILURE`.

### `Selector` *Composite*
Evaluates the children one by one.
Starts next child, if previous child finishes with `FAILURE`.

`SUCCESS`: as soon as a child finishes `SUCCESS`.
`FAILURE`: when all children finished with `FAILURE`.

### `Sequence` *Composite*
Evaluates the children one by one.
Starts next child, if previous child finishes with `SUCCESS`.

`SUCCESS`: when all children finishes `SUCCESS`.
`FAILURE`: as soon as a child finished with `FAILURE`.

### `Timer` *Decorator*
Starts the decorated node.

`SUCCESS`: after x seconds.
`FAILURE`: as soon as decorated node finishes with `FAILURE`.

### `Wrapper` *Decorator*
Always finishes with `SUCCESS`.

### `PlaySound`
*Properties*: `sound`, `volume`

`SUCCESS`: when sound has started playing.
`FAILURE`: otherwise

# Terasology Work module



## Behavior nodes

### `FindWork` *Decorator*
*Properties*: `filter`

Searches for open work of specific type (`filter`). If work is found, the actor is assigned to that and the child is started.

`SUCCESS`: when actor reached a target position.
`FAILURE`: if no open work can be found.

### `FinishWork` *Decorator*
Does the actual work, once the actor is in range. The child node is started.

`SUCCESS`: when work is done (depends on work type).
`FAILURE`: if no work is assigned or target is not reachable.

### `SetTargetToWork`
Set `MinionPathComponent`'s target to the work's target.

`SUCCESS`: if valid work target position found.
`FAILURE`: otherwise

# Terasology Move module

## Behavior nodes

### `FindPathTo`
Requests a path to a target defined using the `MinionPathComponent`.

`SUCCESS` / `FAILURE`: when paths is found or not found (invalid).
`RUNNING`: as long as path is searched.

### `MoveAlongPath` *Decorator*
Call child node, as long as the actor has not reached the end of the path.

`SUCCESS`: when actor has reached end of path.
`FAILURE`: if no path was found previously.

### `FindWalkableBlock` *Decorator*
Searches for the next valid walkable block for pathfinder. Best use of this node is probably
using a `Sequence` on top of the behavior tree and a `MoveTo` as the child.

This node must be run successfully **before** calling `FindPathTo`, because here the start position for pathfinding is queried.

The walkable block is stored into `MinionMoveComponent`.

`SUCCESS`: if the child returns `SUCCESS`.
`FAILURE`: if no walkable block can be found.

### `SetTargetLocalPlayer`
Set `MinionPathComponent`'s target to the block below local player.

Always returns `SUCCESS`.

### `MoveTo`
*Properties:* `distance`

Moves the actor to the target defined by `MinionMoveComponent`.

`SUCCESS`: when distance between actor and target is below `distance`.
`FAILURE`: when there is no target.

### `Jump`
Trigger a single jump into the air.

`SUCCESS`: when the actor is grounded after the jump again.

### `PlayAnimation`
*Properties:* `animation`, `loop`

Play a specific animation. Notice, if `loop` is false, this node will return `RUNNING` forever.

`SUCCESS`: when the animation ended (only if `loop`==false)

## `MinionPathSystem`

The path system helps finding paths and keep track of their current state.

This system listens for `OnPathReady` events and assign `PATH_RECEIVED` state to the minion that requested the path.
To store this state a `MinionPathComponent` is used.

# Terasology Pathfinding module

## Systems

### `PathfinderSystem`

**Events:** `PathReadyEvent`

A system to find paths through the map. Calculations that may take some time are done in background (preprocessing chunks
and finding path).

Once a path for a request is found, a `PathReadyEvent` is sent back to the entity.

If there is a block change, all paths and all nav data associated with the changed chunk is invalidated. If a block
change occurs when there are pending requests the chunk update is processed before any path request.

The nav data (`WalkableBlock` and `Floor`) is *only* modified in the background and *never*, when a path is calculated.

Whenever you operate with nav data, keep in mind that the current instance of a `WalkableBlock` may not be up to date.
Always use `PathfinderSystem.getBlock()` to get the latest block.

# Introduction
The pathfinder system is an attempt to implement a fast and scalable pathfinding algorithm, ready to use to find the shortest path between two given blocks in Terasology.

This system is split into two parts. The first part are the algorithms to preprocess chunks and finding the path itself. The second part forms the API and encapsulate the calculations into a background thread.

This is all about the algorithm.

## Finding paths in general
The probably first and best algorithm to find shortest paths in a graph is the A* (called "A Star"). A* is a concretization of the Dijkstra's algorithm.

The goal of dijkstra is finding the shortest path between a start and a target node. The idea is, to start at the target node and visit each neighbor of the target. The exact costs to walk to that neighbor node is calculated and stored including the current node at that neighbor node. Once the start node is visited, we are able to reconstruct the actual shortest path, by walking backwards to the target node (thats why predecessors are stored...).
A* works exactly the same, but modifies the order in which neighbor nodes are visited, so that the finding need much fewer iterations.

## Finding paths in 3D
A* works wonderful if the number of nodes to visit is known and not that big. In 3D voxel worlds like Terasology, the number of nodes is not known and really big (One chunk: 16x16x256=65536).

Of course, most of those 64k blocks are useless for finding a path, where a player can walk on. The interesting blocks are those, who are solid and have at least two air blocks above. Now, even if the chunk is totally flat and there are no holes, we have 16x16=256 blocks (per chunk!) to consider when path finding. Still too much.

## Preprocessing the world
If we consider the example from above and we have three chunks (A, B and C; each flat, no holes) and we want a path from chunk A to chunk B we could decrease the number of interesting blocks in this way:
* all blocks of A and B are needed (chunk with start and target block)
* for chunk B, only one ("virtual") node is needed (only if there is a path between any block of A and any block of C (through B))

When finding a path in such a reduced setup, we search a path from the start block to the "virtual" node (that represents the complete chunk B) and a path from the virtual node to the target block (chunk C).

Instead of 3x16x16=768 nodes, we only need 2x16x16+1=513 nodes (the longer the path, the bigger the impact). Of course, with this optimization we dont find the shortest path anymore. We find a good approximation in shorter time.

### Sweeps, Regions, Floors, Chunks
Now, we dont have a flat world and of course we have holes and big mining constructions. Additionally, our world may change (often!), so this precalculation needs to be fast. It is done in several steps:

1. Find all walkable blocks by scanning through all blocks of the current chunk (block must be not penetrable with 2 air blocks above)
2. Build a graph of all walkable blocks (find neighbors)
3. Find *sweeps* of walkable blocks (blocks lined up next to each other, without holes or corners)
4. Combine sweeps next to each other into *regions*
5. Build a graph of all regions (find neighbors)
6. Combine regions next to each other and are not overlapping (different heights) into *floors*. Notice: There exists a path from each block to each other block of this floor.
7. Build a graph of all floors (find neighbors)

After this steps we have a graph of floors. Each floor contains a graph of regions. Each region contains a graph of walkable blocks.

## Hierarchical A*
Hierarchical A* works like traveling by plane. You first use the train to reach the local airport. Then you fly to the next bigger airport. Next is oversea. Then to local airport where you take the taxi.

With our multilevel graph we do the same thing. As for A* we start at the target node. First we consider all local neighbors for this node. Next we check, if we are currently at a border to the next region (or floor).
If so, we can reach any other border block of the neighboring region (or floor), so we can "fly" to those border blocks directly.

The actual paths from border block to border block can be precalculated (or at least be cached). We may not consider ALL border blocks of a region/floor. Current implementation uses corners only.

## Theta*
The Theta* algorithm slightly modifies the default A*, in a way to produce more natural paths.

Instead of having every single step in a path, only the the endpoints of lines of sight are stored in a path (basically only the points, where the minion need to turn).

There are two implementations of the line of sight algorithm. One is crawling through walkable block using a 2d bresenham algorithm (recommended). The other uses a 3d bresenham with the real world blocks.

## Related
* http://aigamedev.com/open/tutorials/theta-star-any-angle-paths/

