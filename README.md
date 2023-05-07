# Year 3 course

Informatics Large(Lunch) Practical, Coursework related to pathfinding drone with traveling salesman problem. 

79% overall - part 1 - 22/25 and part 2 - 57/75


# Basic Model

This is an implementation of pathfinding drone, the basic jist of it. 

It indexes a website for orders then looks at various permutations of paths it could take resulting in the optimal order in which it should go about delivery the orders.

The optimal order is decided through traveling salesman, and once the permutation is decided, the drone starts greedy A* between start point and first order. Then first order destination and second order restaurant... so and so forth. This continues until the final order is reached.

The drone tries to find a path between two points A and B while avoiding any No-Fly zones. 
