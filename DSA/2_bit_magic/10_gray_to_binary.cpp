#include<bits/stdc++.h>
using namespace std;


int grayToBinary(int g) {
    int b = g;
    while (g >>= 1) {
        b ^= g;
    }
    return b;
}

int main(int argc, char const *argv[])
{
  
  return 0;
}