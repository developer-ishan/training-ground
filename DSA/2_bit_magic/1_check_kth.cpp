#include<bits/stdc++.h>
using namespace std;

bool isKSet(int n, int k){
  return (n & 1<<k) > 0;
}

int main(int argc, char const *argv[]){

  cout<<isKSet(5, 4)<<"\n";
  cout<<isKSet(5, 3)<<"\n";
  cout<<isKSet(5, 2)<<"\n";
  cout<<isKSet(5, 1)<<"\n";
  cout<<isKSet(5, 0)<<"\n";
  //5 = 101
  return 0;
}