package se.uu.it.cats.brick;

import lejos.nxt.Sound;

public class Music {

	private final static int[] MELODY = {
	// 1
			1, 6, 0, // B
			1, 5, 0, // A
			1, 6, 0, // B
			1, 3, 1, // F#
			1, 1, 0, // D
			1, 3, 1, // F#
			0, 6, 0, // B
			-1, 0, 0, // pause
			1, 6, 0, // B
			1, 5, 0, // A
			1, 6, 0, // B
			1, 3, 1, // F#
			1, 1, 0, // D
			1, 3, 1, // F#
			0, 6, 0, // B
			-1, 0, 0, // pause
			// 2
			1, 6, 0, // B
			2, 0, 1, // C#
			2, 1, 0, // D
			2, 0, 1, // C#
			2, 1, 0, // D
			1, 6, 0, // B
			2, 0, 1, // C#
			1, 6, 0, // B
			2, 0, 1, // C#
			1, 5, 0, // A
			// 3
			1, 6, 0, // B
			1, 5, 0, // A
			1, 6, 0, // B
			1, 4, 0, // G
			1, 6, 0, // B
			-1, 0, 0, // pause
			1, 6, 0, // B
			1, 5, 0, // A
			// 1
			1, 6, 0, // B
			1, 5, 0, // A
			1, 6, 0, // B
			1, 3, 1, // F#
			1, 1, 0, // D
			1, 3, 1, // F#
			0, 6, 0, // B
			-1, 0, 0, // pause
			1, 6, 0, // B
			1, 5, 0, // A
			1, 6, 0, // B
			1, 3, 1, // F#
			1, 1, 0, // D
			1, 3, 1, // F#
			0, 6, 0, // B
			-1, 0, 0, // pause
			// 2
			1, 6, 0, // B
			2, 0, 1, // C#
			2, 1, 0, // D
			2, 0, 1, // C#
			2, 1, 0, // D
			1, 6, 0, // B
			2, 0, 1, // C#
			1, 6, 0, // B
			2, 0, 1, // C#
			1, 5, 0, // A
			// 4
			1, 6, 0, // B
			1, 5, 0, // A
			1, 6, 0, // B
			2, 0, 1, // C#
			2, 1, 0, // D
			-1, 0, 0, // pause
			2, 3, 1, // F#
			2, 2, 0, // E
			// 5
			2, 3, 1, // F#
			2, 1, 0, // D
			1, 5, 0, // A
			2, 1, 0, // D
			1, 3, 1, // F#
			-1, 0, 0, // pause
			2, 3, 1, // F#
			2, 2, 0, // E
			2, 3, 1, // F#
			2, 1, 0, // D
			1, 5, 0, // A
			2, 1, 0, // D
			1, 3, 1, // F#
			-1, 0, 0, // pause
			// 6
			2, 3, 1, // F#
			2, 4, 1, // G#
			2, 5, 0, // A
			2, 4, 1, // G#
			2, 5, 0, // A
			2, 3, 1, // F#
			2, 4, 1, // G#
			2, 3, 1, // F#
			2, 4, 1, // G#
			2, 2, 0, // E
			// 7
			2, 3, 1, // F#
			2, 2, 0, // E
			2, 3, 1, // F#
			2, 1, 0, // D
			2, 3, 1, // F#
			-1, 0, 0, // pause
			2, 3, 1, // F#
			2, 2, 0, // E
			// 5
			2, 3, 1, // F#
			2, 1, 0, // D
			1, 5, 0, // A
			2, 1, 0, // D
			1, 3, 1, // F#
			-1, 0, 0, // pause
			2, 3, 1, // F#
			2, 2, 0, // E
			2, 3, 1, // F#
			2, 1, 0, // D
			1, 5, 0, // A
			2, 1, 0, // D
			1, 3, 1, // F#
			-1, 0, 0, // pause
			// 6
			2, 3, 1, // F#
			2, 4, 1, // G#
			2, 5, 0, // A
			2, 4, 1, // G#
			2, 5, 0, // A
			2, 3, 1, // F#
			2, 4, 1, // G#
			2, 3, 1, // F#
			2, 4, 1, // G#
			2, 2, 0, // E
			// 8
			2, 3, 1, // F#
			2, 2, 0, // E
			2, 1, 0, // D
			2, 2, 0, // E
			2, 3, 1, // F#
			-1, 0, 0, // pause
	};

	private static int[][][] NOTE_FREQ = { { // 3
			{ 262 >> 1, 277 >> 1 }, // C
					{ 294 >> 1, 311 >> 1 }, // D
					{ 330 >> 1, 0 }, // E
					{ 349 >> 1, 370 >> 1 }, // F
					{ 392 >> 1, 415 >> 1 }, // G
					{ 440 >> 1, 466 >> 1 }, // A
					{ 494 >> 1, 0 }, // B
			}, { // 4
			{ 262, 277 }, // C
					{ 294, 311 }, // D
					{ 330, 0 }, // E
					{ 349, 370 }, // F
					{ 392, 415 }, // G
					{ 440, 466 }, // A
					{ 494, 0 }, // B
			}, { // 5
			{ 523, 554 }, // C
					{ 587, 622 }, // D
					{ 659, 0 }, // E
					{ 698, 740 }, // F
					{ 784, 831 }, // G
					{ 880, 932 }, // A
					{ 988, 0 }, // B
			}, { // 6
			{ 1047, 1109 }, // C
					{ 1175, 1245 }, // D
					{ 1319, 0 }, // E
					{ 1397, 1480 }, // F
					{ 1568, 1661 }, // G
					{ 1760, 1865 }, // A
					{ 1976, 0 }, // B
			}, { // 7
			{ 2093, 2217 }, // C
					{ 2349, 2489 }, // D
					{ 2637, 0 }, // E
					{ 2794, 2960 }, // F
					{ 3136, 3322 }, // G
					{ 3520, 3729 }, // A
					{ 3951, 0 }, // B
			}, { // 8
			{ 4186, 4435 }, // C
					{ 4699, 4978 }, // D
					{ 5274, 0 }, // E
					{ 5588, 5920 }, // F
					{ 6272, 6644 }, // G
					{ 7040, 7458 }, // A
					{ 7902, 0 }, // B
			}, };
	public int NOTE_TIME = 200; // Note duration
	public int TONE_TIME = 80; // Tone duration

	private long nexttime;
	private int id, noplayers;

	/**
	 * Simple music player for the cat which plays popcorn by Gershon Kingsley.
	 * The play method needs to be called for playing ot start.
	 * 
	 * @param id
	 *            id of the cat
	 * @param noplayers
	 *            number of cats
	 * @param starttime
	 *            time to start playing at
	 */
	public Music(int id, int noplayers, int starttime) {
		this.id = id;
		this.noplayers = noplayers;
		nexttime = starttime;
	}

	public Music(int id, int noplayers) {
		this.id = id;
		this.noplayers = noplayers;
		
		//nexttime = Clock.timestamp() + 1000;
		int timeNow = Clock.timestamp();
		nexttime = timeNow - (timeNow % 2000) + 7000;
	}

	public Music() {
		id = -1;
		noplayers = 1;
		
		// start on the second start
		// after 5 seconds
		int timeNow = Clock.timestamp();
		nexttime = timeNow - (timeNow % 2000) + 7000;
	}

	/**
	 * Start playing music
	 */
	public void play() {
		// Counter for the notes
		int note_number = 0;
		// Wait for tune to start
		try {
			Thread.sleep(nexttime - Clock.timestamp());
		} catch (Exception e) {
		}
		for (int position = 0; position < MELODY.length / 3; position++) {
			// Get information on the note
			int octave = MELODY[position * 3];
			// Check if there is a note to be played
			if (octave >= 0) {
				// See if this cat should play
				if ((id == -1) || ((note_number % noplayers) == id)) {
					// Play note
					int tone = MELODY[position * 3 + 1];
					int pitch = MELODY[position * 3 + 2];
					int freq = NOTE_FREQ[octave][tone][pitch];
					Sound.playNote(Sound.PIANO, freq, TONE_TIME);
				}
				note_number++;
			}
			// Set up the time of the next note
			nexttime += NOTE_TIME;
			// Wait until next tone
			try {
				Thread.sleep(nexttime - Clock.timestamp());
			} catch (Exception e) {
			}
		}
	}
}