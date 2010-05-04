package se.uu.it.cats.pc.gui;

import java.util.Hashtable;

public class Area { // implements ActionListener
  // Array of cats
  private Cat[] _cats = new Cat[3];
  private Mouse _mouse = new Mouse();
  private Lighthouse[] _lighthouse = new Lighthouse[4];
  // The absolute time
  private int time = 0;
  
  private int arenaWidth = 200;
  private int arenaHeight = 200;
  
  private static Area instanceHolder = new Area(); 
  
  public static Area getInstance()
  {
	  return instanceHolder;
  }
  
  /* Constructor */
  public Area() {
    // Create some stuff
    createEntities();
  }

  /* Creation of mouse, cats and beacons */
  public void createEntities() {

    /* Create 3 cats */
    for(int i = 0; i < 3; i++) {
      _cats[i] = new Cat("cat"+(i+1));
    }
	_cats[0].updateXYAngles(10, 100, 0, 0);
	_cats[1].updateXYAngles(90, 170, 0, 0);
	_cats[2].updateXYAngles(300, 100, 0, 0); // Real camera should go for cats real angle + relative camera angle.
	
    
	_mouse = new Mouse();
	_mouse.newPosition(50, 243);
	
	/* Create four lighthouses */
	_lighthouse[0] = new Lighthouse("Beacon 1");
	_lighthouse[1] = new Lighthouse("Beacon 2");
	_lighthouse[2] = new Lighthouse("Beacon 3");
	_lighthouse[3] = new Lighthouse("Beacon 4");
	
	
	_lighthouse[0].newPosition(0,0);
	_lighthouse[1].newPosition(arenaWidth,0);
	_lighthouse[2].newPosition(arenaWidth,arenaHeight);
	_lighthouse[3].newPosition(0,arenaHeight);
	
  }

  /* Tick master. On call aquires all items in enclosed area. */
  public void tick() {
    time++;
  }
  
  public int getArenaWidth() {
	return arenaWidth;
  }
  public int getArenaHeight() {
	return arenaHeight;
  }
  public void setArenaWidth(int w) {
	  arenaWidth = w;
	  update(); // Update positions of lighthouses
  }
  public void setArenaHeight(int h) {
	  arenaHeight = h;
	  update(); //Update positions of lighthouses
  }
  public Cat[] getCats() {
	return _cats;
  }
  public Mouse getMouse() {
	return _mouse;
  }
  public void update() {
	  _lighthouse[0].newPosition(0,0);
	  _lighthouse[1].newPosition(arenaWidth,0);
	  _lighthouse[2].newPosition(arenaWidth,arenaHeight);
	  _lighthouse[3].newPosition(0,arenaHeight);
  }
  public Lighthouse[] getLighthouse() {
	return _lighthouse;
  }
};
