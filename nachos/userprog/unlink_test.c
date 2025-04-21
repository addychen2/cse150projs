/*
 * unlink_test.c
 *
 * Test the unlink system call under different conditions
 * and verify its functionality.
 *
 * This test will:
 * 1. Create a file
 * 2. Perform `unlink` to remove it
 * 3. Try to access the file after unlink to check if it was removed successfully
 *
 * Geoff Voelker
 * Date: [Insert Date]
 */
#include "../test/syscall.h"
#include "../test/stdio.h"  
#include <stdlib.h>


int main(int argc, char *argv[]) {
    char *fileName = "testfile.txt";

    // Step 1: Create the file
    int fd = creat(fileName);
    if (fd < 0) {
        printf("Failed to create the file.\n");
        exit(-1);
    }

    // Step 2: Perform the unlink operation to remove the file
    int unlinkResult = unlink(fileName);
    if (unlinkResult == 0) {
        printf("File '%s' unlinked successfully.\n", fileName);
    } else {
        printf("Failed to unlink the file '%s'.\n", fileName);
        exit(-1);
    }

    // Step 3: Try to open the file after unlink to check if it's removed
    fd = open(fileName);
    if (fd < 0) {
        printf("File '%s' is successfully removed (cannot open it).\n", fileName);
    } else {
        printf("File '%s' still exists, which is an error.\n", fileName);
        exit(-1);
    }

    return 0;
}
