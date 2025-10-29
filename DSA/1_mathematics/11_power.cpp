#include<bits/stdc++.h>
using namespace std;

int power(int x, int n){
  if(n%2 == 0){
    int temp = power(x, n/2);
    return temp*temp;
  }
  return x*power(x, n-1);
}

int pow_itr(int x, int n){
  int res = 1;
  while(n>0){
    if(n%2!=0)
      res=res*x;
    x=x*x;
    n=n/2;
  }
  return res;
}
int main(int argc, char const *argv[])
{
  cout<<pow(2,5)<<"\n";
  cout<<pow_itr(2,5)<<"\n";
  return 0;
}