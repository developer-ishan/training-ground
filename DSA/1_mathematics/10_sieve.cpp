#include<bits/stdc++.h>
using namespace std;

void print_primes(int n){
  vector<bool> is_prime(n, true);
  for(int i=2; i<n; i++){
    if(is_prime[i]){
      for(int j=2; i*j<n; j++){
        is_prime[i*j] = false;
      }
    }
  }

  for(int i=2; i<n; i++){
    if(is_prime[i])
      cout<<i<<" ";
  }
  cout<<"\n";
}
int main(int argc, char const *argv[])
{
  print_primes(10);
  return 0;
}