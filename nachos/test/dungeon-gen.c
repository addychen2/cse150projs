/*
 * dungeon-gen.c
 *
 * Unobfuscated C program for creating a random dungeon layout.
 *
 * Robert Nystrom
 * March 2019
 *
 * https://gist.github.com/munificent/ce8f7a9e6b09938ca8d2d43fa62f9864
 */

#include "stdio.h"
#include "stdlib.h"

#define H 24 // const int H = 40;
#define W 80 // const int W = 80;

char map[H][W];

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
  
  if (doorCount > 0) map[dy][dx] = '+';
  
  for (int i = 0; i < (start ? 1 : rnd(6) + 1); i++) {
    int thing = start ? '@' : rnd(4) == 0 ? '$' : 65 + rnd(62);
    map[rnd(h) + ry][rnd(w) + rx] = thing;
  }
}

int main(int argc, const char * argv[]) {
  //srand(time(NULL));
  // nachos needs help with a random seed
  char buf[16];
  int seed = 3;

  printf ("Enter a random number: ");
  readline(buf, 16);
  seed = atoi(buf);
  srand((unsigned int) seed);

  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      map[y][x] = ' ';
    }
  }
  
  addRoom(1);
  for (int i = 0; i < 5000; i++) {
    addRoom(0);
  }

  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      char c = map[y][x];
      putchar(c == '!' ? '#' : c);
      if (x == W - 1) printf("\n");
    }
  }

  return 0;
}
