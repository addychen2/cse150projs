/*
 * dungeon-obf.c
 *
 * Obfuscated C program for creating a random dungeon layout.
 *
 * Robert Nystrom
 * March 2019
 *
 * https://gist.github.com/munificent/b1bcd969063da3e6c298be070a22b604
 *
 */

// #include "time.h"  Robert Nystrom
#include "stdio.h" // @munificentbob
#include "stdlib.h" //     for Ginny
#define  r return    //    2008-2019
#define  l(a, b, c, d) for (i y=a;y\
<b; y++) for (int x = c; x < d; x++)
typedef int i;const i H=24;const i W
=80;i m[40][80];i g(i x) {r rand()%x;}
void cave(i s){i w=g(10)+5;i h=g(6)+
3;i t=g(W-w-2)+1;i u=g(H-h-2)+1;l(u-
1,u+h+2,t-1,            t+w+2)if(m[y
][x]=='.')                  r;i d=0;
i e,f;        if(!s){l(u      -1,u+h
+2,t-1    ,t+w+2){i s=x<t||     x>t+
w; i    t=y<u||           y>    u+h;
if(s    ^t&&              m[      y]
[x    ]=='#'    ){d++;    if(g    (d
)     ==0)    e=x,f=y;    }}if    (d
==    0)r;    }l(u-1,u    +h+2    ,t
-1    ,t+w    +2){i s=    x< t    ||
x>    t+w;    i t= y<u    ||y>    u+
h;    m[y]      [x]= s    &&t?   '!'
:s^t    ?'#'                    :'.'
;}if    (d>0)m                  [f][
e]=g(2    )?'\'':'+';for(i j=0;j<(s?
1:g(6)        +1);j++)m[g(h)+u][g(w)
+t]=s?'@'                 :g(4) ==0?
'$':65+g(62)              ;}i main(i
argc, const char* argv[]) {srand((i)
93186752);  l(0, H, 0,W)m[y][x]=' ';
for(i j=0;j<1000;j++)cave(j==0);l(0,
H,0,W) {i c=m[y][x]; putchar(c=='!'?
'#':c);if(x==W-1)printf("\n");}r 0;}
