#include<bits/stdc++.h>
using namespace std;

void print(int n){
  if(n==0){
    return;
  }
  print(n-1);
  cout<<n<<" ";
}
int main(int argc, char const *argv[])
{
  print(10);
  return 0;
}