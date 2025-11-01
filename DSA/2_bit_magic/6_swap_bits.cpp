#include <bits/stdc++.h>
using namespace std;

// Given an unsigned integer n, You have to swap all even-position bits with their rightside adjacent odd-position bits.
unsigned int swapBits(unsigned int n){
  // Mask even bits (bit positions 1,3,5,...)
  unsigned int even_bits = n & 0xAAAAAAAA;
  // Mask odd bits (bit positions 0,2,4,...)
  unsigned int odd_bits = n & 0x55555555;

  // Shift even bits right by 1, odd bits left by 1
  even_bits >>= 1;
  odd_bits <<= 1;

  // Combine the shifted bits
  return (even_bits | odd_bits);
}
int main(int argc, char const *argv[]){

  return 0;
}