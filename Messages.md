#Messages that need to be sent over the network.

# Messages for the Unscented Kalman Filter #

Probably only one big message needed:

{id (int), t (int), angle\_m (f), x\_m (f), y\_m (f), v\_xm (f), v\_ym (f), x\_c (f), y\_c (f), v\_xc (f), vy\_m (f), angle\_c (f), angle\_cam (f), seen\_landmarks (int(4)), sees\_mouse (boolean) }

Notes:

  * id = id of the cat
  * t = time stamp of when angle\_m was measured
  * angle\_m = angle measurement of the mouse (relative to the map)
  * x\_m = x position of the mouse
  * y\_m = y position of the mouse
  * v\_xm = x component of velocity of the mouse
  * v\_ym = y component of velocity of the mouse
  * x\_c = x position of the cat
  * y\_c = y position of the cat
  * v\_xc = x component of velocity of the cat
  * v\_yc = y component of velocity of the cat
  * angle\_c = orientation of the cat (relative to the map)
  * angle\_cam = orientation of the camera relative to the map
  * seen\_landmarks = landmarks currently within the view of the cat
  * sees\_mouse = if the cat currently sees the mouse


# Messages for the Particle Filter #

Send measurement:

{id (int), t (int), angle\_m (f), x\_c (f), y\_c (f)}

Notes:
Will be sent by each cat during each iteration

Send result:

{id (int), t (int), x\_m (f), y\_m (f), v\_xm (f), v\_ym (f), covar (f(6)) weight (f)}