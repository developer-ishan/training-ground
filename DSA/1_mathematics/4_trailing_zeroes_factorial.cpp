#include<bits/stdc++.h>
using namespace std;

int count_trailing_zeroes(int n){
  int ans = 0;
  for(int i=5; i<=n; i*=5){
    ans += n/i;
  }
  return ans;
}

int main(int argc, char const *argv[])
{
  int n;
  cin>>n;
  cout << count_trailing_zeroes(n) << "\n"; 
  return 0;
}