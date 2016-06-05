https://cacoo.com/diagrams/huo1Fw2Xvwfy6q1N

# Terasology Work module

`Work` in terms of this document is a process that a minion can operate on. Every `Work` is designated to a
 target. This may be any entity (normal or block entity). This is done using the `WorkTargetComponent` - entities having
 this component are targets of some work to do (for example, kill or build block).

To start operating, several preconditions must be fulfilled (most commonly this is the exact positions to operate). The
 definition of those preconditions and the process itself is done in a `WorkType`.

A `WorkType` is a component system which registers itself to the `WorkFactory`. So, modules can bring their own work
 type implementations into the system.

The central system of the work module is the `WorkBoard`. This systems manages all the work.

Internally a hierarchical k-means clustering algorithm is used to build a tree of clusters, to minimize the search space
 when looking for work for a minion. The `WorkBoard` hides this complexity - you simply request for work described by
 optional filters. Once work becomes available, you get informed through a callback.

For now, there are 4 `WorkTypes`:

*   `BuildBlock` - go to a target and place a block
*   `RemoveBlock` - go to a target and remove a block
*   `WalkToBlock` - just go to a target and do nothing
*   `AttackMinionSystem` - spawns 2 additional work types, one for each LightAndShadow team. Also tags entities with the
    `WorkTargetComponent` and a attack work type of the opposing team side. (WIP)

## Example `Work`

<pre>
@RegisterSystem
public class WalkToBlock implements Work, ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(WalkToBlock.class);

    private final SimpleUri uri;
    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private WorkFactory workFactory;

    public WalkToBlock() {
        uri = new SimpleUri("Pathfinding:walkToBlock");
    }

    @Override
    public void initialise() {
        // register this work type to the factory
        workFactory.register(this);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }

    public List<WalkableBlock> getTargetPositions(EntityRef block) {
        // returns a list of valid blocks to operate on block
        return ...;
    }

    @Override
    public boolean canMinionWork(EntityRef block, EntityRef minion) {
        // return true if minion can work on block, right now
        return ...;
    }

    @Override
    public boolean isAssignable(EntityRef block) {
        // return true if block is valid target. only valid block
        // will be considered when work is searched
        return ...;
    }

    @Override
    public void letMinionWork(EntityRef block, EntityRef minion) {
        // do the actual work
        block.removeComponent(WorkTargetComponent.class);
    }

    @Override
    public boolean isRequestable(EntityRef block) {
        // return true is block can be worked on right now (all preconditions are true)
        return ...;
    }

    @Override
    public float cooldownTime() {
        // returns the seconds it takes to finish working
        return 0;
    }

    @Override
    public String toString() {
        return "Walk To Block";
    }
}
</pre>

## Behavior nodes

### `FindWork`
*Properties*: `filter`

Searches for open work of specific type (`filter`). If work is found, the actor is assigned.

`SUCCESS`: When work is found and assigned.

### `FinishWork` *Decorator*
Does the actual work, once the actor is in range. The child node is started.

`SUCCESS`: when work is done (depends on work type).
`FAILURE`: if no work is assigned or target is not reachable.

### `SetTargetToWork`
Set `MinionMoveComponent`'s target to the work's target.

`SUCCESS`: if valid work target position found.
`FAILURE`: otherwise

# Terasology Move module

## Behavior nodes

### `FindPathTo`
Requests a path to a target defined using the `MinionMoveComponent.target`.

`SUCCESS` / `FAILURE`: when paths is found or not found (invalid).
`RUNNING`: as long as path is searched.

### `SetTargetToNearbyBlockNode`
Sets the target to a random nearby block.

`SUCCESS`: when a target has been chosen.
`FAILURE`: when no target could be chosen.

### `MoveAlongPath` *Decorator*
Call child node, as long as the actor has not reached the end of the path. Sets `MinionMoveComponent.target` to next step in path.

`SUCCESS`: when actor has reached end of path.
`FAILURE`: if no path was found previously.

### `SetTargetLocalPlayer`
Set `MinionMoveComponent.target` to the block below local player.

Always returns `SUCCESS`.

### `MoveTo`
*Properties:* `distance`

Moves the actor to the target defined by `MinionMoveComponent`.

`SUCCESS`: when distance between actor and target is below `distance`.
`FAILURE`: when there is no target.

### `Jump`
Trigger a single jump into the air.

`SUCCESS`: when the actor is grounded after the jump again.

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


# Terasology Behavior Tree

Behavior trees form an API to everything AI/behavior related. They consist of nodes ordered in a strong hierarchical form.

Nodes run isolated code to either read or write values from/to the game world. Each behavior tree can be run by multiple
 `Actors`. To keep the internal state of the tree for each actor, nodes are not run directly. Instead for each node to run
 by an `Actor`, the node creates a `Task`.

`Tasks` are scheduled in a way that they can inform parent `Tasks` using an observer pattern. The management of the
state is maintained by an `Interpreter`.

The `Interpreter` keeps a list of active `Tasks`. Tasks are considered active, when the currently return `RUNNING` state.
Each tick, the `update` methods is called for all active tasks.

`Nodes` are identified using behavior prefabs. There some general settings may be applied.

## Example Node and Task

<pre>
import org.terasology.rendering.nui.properties.Range;

public class TimerNode extends DecoratorNode {
    @Range(min = 0, max = 20)
    private float time;  // this becomes a slider in the property editor for this node

    @Override
    public Task createTask() {
        // tasks are injected, so using @In is possible in the task class
        return new TimerTask(this);
    }

    public static class TimerTask extends DecoratorTask {
        private float remainingTime; // store state for this task

        public TimerTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            // is called once, directly before the first update() call

            remainingTime = getNode().time; // init state
            start(getNode().child); // start the associated child, if any
        }

        @Override
        public Status update(float dt) {
            // is called in each tick of the behavior tree, if this task remains active

            remainingTime -= dt;
            if (remainingTime <= 0) {
                // once the time is up, this node stops with FAILURE
                return Status.FAILURE;
            }
            return Status.RUNNING; // remain active otherwise
        }

        @Override
        public void handle(Status result) {
            // is called when the state of the started child node changes to SUCCESS or FAILURE

            stop(result); // stop this task and all task, that were started within the task
        }

        @Override
        public TimerNode getNode() {
            return (TimerNode) super.getNode();
        }
    }
}
</pre>

## Example node prefab

<pre>
{
    "BehaviorNode" : {
        "type"      : "TimerNode",
        "name"      : "Timer",
        "category"  : "logic",
        "shape"     : "rect",
        "description": "Decorator\nStarts the decorated node.\nSUCCESS: as soon as decorated node finishes with SUCCESS.\nFAILURE: after x seconds.",
        "color"     : [180, 180, 180, 255],
        "textColor" : [0, 0, 0, 255]
    }
}
</pre>

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

`SUCCESS`: as soon as decorated node finishes with `SUCCESS`.
`FAILURE`: after x seconds.

### `Wrapper` *Decorator*
Always finishes with `SUCCESS`.

### `PlaySound`
*Properties*: `sound`, `volume`

`RUNNING`: while sound is playing
`SUCCESS`: once sound ends playing
`FAILURE`: otherwise

### `PlayMusic`
*Properties*: `music`

`RUNNING`: while music is playing
`SUCCESS`: once music ends playing
`FAILURE`: otherwise

##
