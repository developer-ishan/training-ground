#include<bits/stdc++.h>
using namespace std;

bool is_prime(int n){
  if(n==1)
    return false;

  for(int i=2; i*i<=n; i++){
    if(n%i == 0)
      return false;
  }
  return true;
}
int main(int argc, char const *argv[])
{
  cout<<is_prime(2)<<"\n";
  cout<<is_prime(3)<<"\n";
  cout<<is_prime(5)<<"\n";
  cout<<is_prime(10)<<"\n";
  return 0;
}