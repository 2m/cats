package se.uu.it.cats.pc.actor;

import se.uu.it.cats.brick.Settings;

public class Area { // implements ActionListener
	
	public static int CAT_COUNT = 3;
	public static int LIGHTHOUSE_COUNT = 4;
	
	// Array of cats
	private Cat[] _cats = new Cat[CAT_COUNT];
	private Mouse _mouse = new Mouse();
	private Lighthouse[] _lighthouse = new Lighthouse[LIGHTHOUSE_COUNT];
	// The absolute time
	private int time = 0;

	private int arenaWidth = (int)((Settings.ARENA_MAX_X - Settings.ARENA_MIN_X) * 100);
	private int arenaHeight = (int)((Settings.ARENA_MAX_Y - Settings.ARENA_MIN_Y) * 100);

	private static Area instanceHolder = new Area(); 

	public static Area getInstance()
	{
		return instanceHolder;
	}

	/* Constructor */
	private Area() {
		// Create some stuff
		createEntities();
	}

	/* Creation of mouse, cats and beacons */
	public void createEntities() {

		/* Create 3 cats */
		for(int i = 0; i < 3; i++) {
			Settings.init(i);
			_cats[i] = new Cat("cat"+i, Settings.START_X, Settings.START_Y);
		}
		
		// Real camera should go for cats real angle + relative camera angle.

		_mouse = new Mouse();
		_mouse.newPosition(0, 0);

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

	/* Tick master. On call acquires all items in enclosed area. */
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

	public Cat getCat(int id) {
		return _cats[id];
	}

	public String[] getCatNames() {
		String[] names = new String[_cats.length];
		for (int i = 0; i < _cats.length; i++)
			names[i] = _cats[i].getName();
		return names; 
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
