# Project Boards
*A Project Management mod for Minecraft*
## Summary
Project Boards is a mod that lets you organize projects by planning, starting, and finishing tasks using collections of Task Boards.

## Task Boards
You can place down Task Boards which let you display text in the world.
These work a lot like Signs, but the text is stored in an item called a Task Slip.  
The name of the Task Slip item is what is shown on the Task Board.
You can right-click the task board with a Task Slip to swap it for the slip inside.
When you interact with an empty Task Board with a slip in your hand it gets inserted and the text appears.
You can shift right-click a Task Board to take out the Task Slip inside.
When you break a Task Board, the Task Slip inside will disappear.
This is intentional, since Task Boards create new slips during use. 
### Task Owner
You can right-click the space above the task's text on a Task Board to put your name down as the owner.
You can shift right-click the same spot to remove the owner (whether it was you or another player).
### Task Status
You can right-click the space below the task's text on a Task Board to advance its status from Planned to Started, or 
from Started to Completed.
You can shift right-click the same spot to move the status backwards instead.

## TODO for v0.1
- [x] Add recipe for Task Boards
- [x] UI for editing a Task by clicking on the task board with an empty hand
- [x] Text wrapping on Task Boards
- [x] Ability to change owner & status of a task on a task board
- [x] Text displaying the status of a task: 'planned', 'started', or 'finished'
- [x] Text displaying the 'owner' of a task
- [x] Item transfer support for Task Boards, so hoppers, droppers, and modded blocks can interact with them
- [x] Disallow interacting with a Task Board while another player is editing it
- [x] Fix outstanding bugs
