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
int main(int argc, char const *argv[]){
  cout<<count_set_bits(5)<<"\n";
  return 0;
}