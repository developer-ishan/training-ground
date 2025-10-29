#include<bits/stdc++.h>
using namespace std;

void print_divisors_(int n){
  for(int i=2; i*i<=n; i++){
    if(n%i == 0){
      cout<<i<<" "<<n/i<<" ";
    }
  }
  cout<<"\n";
}

void print_divisors(int n){
  int i=1;
  for(;i*i<n; i++){
    if(n%i == 0){
      cout<<i<<" ";
    }
  }
  for(;i>=1; i--){
    if(n%i == 0)
      cout<<n/i<<" ";
  }
  cout<<"\n";
}


int main(int argc, char const *argv[])
{
  print_divisors(100);
  return 0;
}