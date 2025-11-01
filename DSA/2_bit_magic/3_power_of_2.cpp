#include<bits/stdc++.h>
using namespace std;

int count_set_bits(int n){
  int cnt = 0;
  while(n!=0){
    if(n&1){
      cnt++;
    }
    n = n>>1;
  }
  return cnt;
}

bool power_2(int n){
  if(count_set_bits(n) == 1)
    return true;
  return false;
}

bool power_2_efficient(int n){
  return n & (n-1) == 0;
}
int main(int argc, char const *argv[]){
  cout<<power_2(1)<<"\n";
  cout<<power_2(2)<<"\n";
  cout<<power_2(16)<<"\n";
  cout<<power_2(14)<<"\n";
  return 0;
}