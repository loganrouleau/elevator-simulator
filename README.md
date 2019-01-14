### Elevator Simulator

How are the control systems in an elevator programmed? Here I use reinforcement learning to try
to derive optimal control patterns.

### Method

I'm using the Q-learning algorithm to train the elevator. The elevator is modelled to know 4 pieces of information:
* Current position along it's linear axis.
* Which floors have people currently waiting.
* How long people at each floor have been waiting in seconds.
* How many people are currently in the elevator.

At any decision point, the elevator can only take 3 actions: move up, move down, or stop.

Assumptions: 
* There is a single elevator in a building with 4 floors and a capacity of 3 people.
* There is only a down button on each floor (down-peak simulation).
* Only a single person presses the button at a given floor. No additional people arrive while the button is pressed.
This means that the elevator knows exactly how many people are on board at all times, which is not true in reality. 
Perhaps if the weight of the load was known then an estimate could be made.
* People have a equal probability of arriving at any floor.
* There is no acceleration. Adjustments to speed are instant.

Only negative rewards are issued, equal to the sum of the squared wait times per floor. This incentivizes the elevator
to reduce the number of extremely long wait times, at the potential cost of 

### Results
I haven't run many training simulations yet, but have noticed one interesting pattern. In the 4 floor model the elevator
tends to remain at the top floor until 1 or 2 people are waiting, then collect them on the way down and move immediately
back to the top. The rate at which people arrive has a noticeable effect on this pattern. If I increase the rate, the
elevator will wait at the top floor until all 4 floors have people waiting, and then collect them all on the way down.

This makes me wonder if real elevator control systems could benefit from a learning system in their feedback, as the
optimal strategy may depend on external factors which vary by time of day, season, or as large companies on key floors 
go out of business.

### UI

I included a UI to make training and debugging easier. This also gave me a way to visually validate that environmental
constraints are obeyed.

<img src="/demo/demo.png">
