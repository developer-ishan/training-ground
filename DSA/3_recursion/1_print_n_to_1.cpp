#include<bits/stdc++.h>
using namespace std;

void print(int n){
  if(n==0){
    cout<<"\n";
    return;
  }
  cout<<n<<" ";
  print(n-1);
}
int main(int argc, char const *argv[])
{
  print(10);
  return 0;
}