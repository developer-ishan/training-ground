#include<bits/stdc++.h>
using namespace std;

int gcd_naive(int a, int b){
  int gcd = min(a,b);
  while (a%gcd!=0 || b%gcd!=0){
    gcd--;
  }
  return gcd;
}

/**
 * euclidea algo
 * gcd(a,b) = gcd(a-b, b)
 */
int gcd(int a, int b){
  int mx = max(a,b);
  int mn = min(a,b);
  if(mx%mn == 0)
    return mn;
  return gcd(mx-mn, mn);
}

int gcd_itr(int a, int b){
  while(a!=b){
    if(a>b)
      a = a-b;
    else
      b = b-a;
  }
  return a;
}

int gcd_op(int a, int b){
  if(b==0)
    return a;
  return gcd(b, a%b);
}
int main(int argc, char const *argv[])
{
  cout<<gcd_naive(4, 6)<<"\n";
  cout<<gcd(6, 4)<<"\n";
  cout<<gcd_itr(6, 4)<<"\n";
  cout<<gcd_op(6, 4)<<"\n";
  return 0;
}