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


void naive(int n){
  for(int i=2; i<=n; i++){
    if(is_prime(i)){
      int x = i;
      while(n%x == 0){
        cout<<x<<" ";
        x*=i;
      }
    }
  }
  cout<<"\n";
}

void print_prime_factors(int n){
  if(n<=1)
    return;
  for(int i=2; i*i<=n; i++){
    while(n%i == 0){
      cout<<i<<" ";
      n/=i;
    }
  }
  //occurs if last prime factor appears only once
  if(n>1)
    cout<<n<<" ";
  cout<<"\n";
}

void print_prime_factors_efficient(int n){
  if(n<=1) return;
  while(n%2 == 0){
    cout<<2<<" ";
    n/=2;
  }

  while(n%3 == 0){
    cout<<3<<" ";
    n/=3;
  }

  for(int i=5; i*i<=n; i+=6){
    while(n%i == 0){
      cout<<i<<" ";
      n/=i;
    }

    while(n%(i+2) == 0){
      cout<<i+2<<" ";
      n/=(i+2);
    }
  }

  if(n/3)
    cout<<n<<" ";
  cout<<"\n";
}


int main(int argc, char const *argv[])
{
  naive(10);
  naive(7);

  print_prime_factors(450);
  print_prime_factors_efficient(450);
  return 0;
}