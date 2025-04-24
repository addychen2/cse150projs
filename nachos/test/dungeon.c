/*
 * dungeon.c
 *
 * Unobfuscated C program for creating a random dungeon layout.
 *
 * Robert Nystrom
 * March 2019
 *
 * https://gist.github.com/munificent/ce8f7a9e6b09938ca8d2d43fa62f9864
 *
 * + + + + + + + +
 *
 * Extended to interactively explore the dungeon.
 *
 * Geoff Voelker
 * November 2019
 */

#define NACHOS
#ifdef NACHOS
#include "stdio.h"
#include "stdlib.h"
#else
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#endif

#define H 24 // const int H = 40;
#define W 80 // const int W = 80;

char map[H][W];
char view[H][W];

#define UP 0
#define DOWN 1
#define LEFT 2
#define RIGHT 3
#define DIRECTIONS 4

#define FLOOR '.'
#define WALL '#'
#define DOOR '+'
#define PLAYER '@'
#define STAIR '>'

char cmd[16];

#define WRITE(_cmd, _len) write(1, _cmd, _len)

#define CMD3(_a, _b, _c) cmd[0] = 0x1B; cmd[1] = _a; cmd[2] = _b; cmd[3] = _c; cmd[4] = '\0'; WRITE(cmd, 5);
#define CMD7(_a, _b, _c, _d, _e, _f, _g) cmd[0] = 0x1B; cmd[1] = _a; cmd[2] = _b; cmd[3] = _c; cmd[4] = _d; cmd[5] = _e; cmd[6] = _f; cmd[7] = _g; cmd[8] = '\0'; WRITE(cmd, 9)

#define emitchar(_a) cmd[0] = _a; cmd[1] = '\0'; WRITE(cmd, 2)
void emitstr (const char *str) { while (*str) { emitchar (*str++); } }

void
position (char row, char col)
{
  int rowh, rowl, colh, coll;

  /* split into two digits, convert to ascii digits */
  rowh = row / 10; rowl = row % 10; 
  rowh = '0' + rowh; rowl = '0' + rowl;
  colh = col / 10; coll = col % 10; 
  colh = '0' + colh; coll = '0' + coll;

  CMD7('[', rowh, rowl, ';', colh, coll, 'H');
}

typedef struct player_t {
    int row, col;
    int loot;
} player_t;

int startx, starty;
int stairx, stairy;

#define clear_screen() CMD3('[', '2', 'J'); 

void
flash (char *str, int row, int col)
{
    int i = 0;

    CMD3('[', '7', 'm');
    position (row, col);
    emitstr (str);
    while (i < 500000) { i++; }
    CMD3('[', '0', 'm');
    position (row, col);
    emitchar ('@');
}

int rnd(int max) {
    return rand() % max;
}

void addRoom(int start) {
    int w = rnd(10) + 5;
    int h = rnd(6) + 3;
    int rx = rnd(W - w - 2) + 1;
    int ry = rnd(H - h - 2) + 1;

    // See if it's blocked or allowed.
    for (int y = ry - 1; y < ry + h + 2; y++) {
	for (int x = rx - 1; x < rx + w + 2; x++) {
	    if (map[y][x] == '.') return;
	}
    }
  
    int doorCount = 0;
    int dx, dy;
    if (!start) {
	int canPlace = 0;
	for (int x = rx; x < rx + w; x++) {
	    if (map[ry - 1][x] == '#') {
		canPlace = 1;
		doorCount++;
		if (rnd(doorCount) == 0) {
		    dx = x;
		    dy = ry - 1;
		}
	    }
	    if (map[ry + h][x] == '#') {
		canPlace = 1;
		doorCount++;
		if (rnd(doorCount) == 0) {
		    dx = x;
		    dy = ry + h;
		}
	    }
	}
	for (int y = ry; y < ry + h; y++) {
	    if (map[y][rx - 1] == '#')  {
		canPlace = 1;
		doorCount++;
		if (rnd(doorCount) == 0) {
		    dx = rx - 1;
		    dy = y;
		}
	    }
	    if (map[y][rx + w] == '#')  {
		canPlace = 1;
		doorCount++;
		if (rnd(doorCount) == 0) {
		    dx = rx + w;
		    dy = y;
		}
	    }
	}
    
	if (doorCount == 0) return;
    }
  
    for (int y = ry; y < ry + h; y++) {
	for (int x = rx; x < rx + w; x++) {
	    map[y][x] = '.';
	}
    }
    for (int x = rx; x < rx + w; x++) {
	map[ry - 1][x] = '#';
	map[ry + h][x] = '#';
    }
    for (int y = ry; y < ry + h; y++) {
	map[y][rx - 1] = '#';
	map[y][rx + w] = '#';
    }
    map[ry - 1][rx - 1] = map[ry + h][rx - 1] = '!';
    map[ry - 1][rx + w] = map[ry + h][rx + w] = '!';
  
    if (doorCount > 0) map[dy][dx] = DOOR;
  
    for (int i = 0; i < (start ? 1 : rnd(4) + 1); i++) {
//    for (int i = 0; i < (start ? 1 : rnd(6) + 1); i++) {
//	int thing = start ? '@' : rnd(4) == 0 ? '$' : 65 + rnd(62);
//	map[rnd(h) + ry][rnd(w) + rx] = thing;
	if (start) {
	    starty = rnd(h) + ry;
	    startx = rnd(w) + rx;
	} else if (stairx == 0) {
	    stairx = rnd(h) + ry;
	    stairy = rnd(w) + rx;
	    map[rnd(h) + ry][rnd(w) + rx] = STAIR;
	} else {
	    map[rnd(h) + ry][rnd(w) + rx] = '$';
	}
    }
}

void room_reveal (int row, int col) {
    // already printed
    if (view[row][col])
	return;
    // outside the screen
    if (row < 0 || col < 0 || row >= H || col >= W)
	return;

    position (row, col);
    if (map[row][col] == '!')
	map[row][col] = WALL;
    emitchar (map[row][col]);
    view[row][col] = map[row][col];
    
    // recurse on adjacent tiles if on a floor tile
    if ((map[row][col] != FLOOR) &&
	(map[row][col] != PLAYER) &&
	(map[row][col] != '$'))
	return;
    for (int r = -1; r <= 1; r++) {
	for (int c = -1; c <=1; c++) {
	    room_reveal (row + r, col + c);
	}
    }
}

void dungeon_create () {
    stairx = 0;
    stairy = 0;
    
    for (int y = 0; y < H; y++) {
	for (int x = 0; x < W; x++) {
	    map[y][x] = ' ';
	    view[y][x]= '\0';
	}
    }
  
    addRoom(1);
    for (int i = 0; i < 1000; i++) {
	addRoom(0);
    }
}

void dungeon_init () {
    // nachos needs help with a random seed
    // srand(time(NULL));
    char buffer[16];
    int i, seed = 3;

    printf ("Use a|w|s|d or the arrow keys to move, q to quit.  ");
    printf ("Enter a random number: ");
    readline (buffer, 16);
    seed = atoi (buffer);
    srand ((unsigned int) seed);
}

void ui_draw (player_t *p) {
    char buf[16];

    position (H - 1, 0);
    emitstr ("Triton Cash: ");
    sprintf (buf, "%d", p->loot);
    emitstr (buf);
}

void player_init (player_t *p) {
    p->row = starty;
    p->col = startx;
    
    room_reveal (starty, startx);
}

int player_move (player_t *p, int dir)
{
    char old = map[p->row][p->col];

    if (dir == UP) {
	if (p->row == 0)
	    return 0;
	if (map[p->row - 1][p->col] == WALL)
	    return 0;
	p->row--;
    } else if (dir == DOWN) {
	if (p->row == (H - 1))
	    return 0;
	if (map[p->row + 1][p->col] == WALL)
	    return 0;
	p->row++;
    } else if (dir == LEFT) {
	if (p->col == 0)
	    return 0;
	if (map[p->row][p->col - 1] == WALL)
	    return 0;
	p->col--;
    } else if (dir == RIGHT) {
	if (p->col == (W - 1))
	    return 0;
	if (map[p->row][p->col + 1] == WALL)
	    return 0;
	p->col++;
    }

    if (old == DOOR)
	room_reveal (p->row, p->col);

    if (map[p->row][p->col] == STAIR) {
	char c;

	position (H - 1, 0);
	emitstr ("Descend stairs to next level? ");
	c = getchar ();
	position (H - 1, 0);
	emitstr ("                              ");

	if (c == 'y' || c == 'Y')
	    return 1;
	return 0;
    }

    if (map[p->row][p->col] == '$') {
	map[p->row][p->col] = '.';
	view[p->row][p->col] = '.';
	flash ("@", p->row, p->col);
	p->loot += 25 + rnd(25);
    }
    
    return 0;
}

void player_erase (player_t *p)
{
    position (p->row, p->col);
    emitchar (map[p->row][p->col]);
}

void player_draw (player_t *p) {
    int x, y;

/*
    for (y = p->row - 1; y <= p->row + 1; y++) {
	for (x = p->col - 1; x <= p->col + 1; x++) {
	    char c = map[y][x];
	    position (y, x);
	    emitchar(c == '!' ? '#' : c);
	}
    }
*/

    x = p->col; y = p->row;
    position (y, x);
    emitchar('@');
}

void player_loop (player_t *player) {
    do {
	char buf[10];
	int r = 0, c;

	ui_draw (player);
	player_draw (player);

	buf[0] = getchar ();
	if (buf[0] <= 0)
	    break;

	player_erase (player);
	c = buf[0];
	if (c == 'a' || c == 0x44) {
	    r = player_move (player, LEFT);
	} else if (c == 'w' || c == 0x41) {
	    r = player_move (player, UP);
	} else if (c == 'd' || c == 0x43) {
	    r = player_move (player, RIGHT);
	} else if (c == 's' || c == 0x42) {
	    r = player_move (player, DOWN);
	} else if (c == 'q') {
	    position (H, 0);
	    exit (-1);
	}

	if (r == 1)
	    break;
    } while (1);
}

int main(int argc, const char * argv[]) {
    player_t player_data, *player = &player_data;

    dungeon_init();
    
    player->loot = 0;
    while (1) {
	dungeon_create();
	clear_screen();
	player_init(player);
	player_loop(player);
    }

    return 0;
}
