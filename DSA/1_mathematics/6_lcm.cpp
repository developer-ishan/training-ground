#include<bits/stdc++.h>
using namespace std;

int gcd(int a, int b){
  if(b==0)
    return a;
  return gcd(b, a%b);
}

int lcm(int a, int b){
  return a*b/gcd(a,b);
}
int main(int argc, char const *argv[])
{
  cout<<lcm(4, 6)<<"\n"; 
  return 0;
}