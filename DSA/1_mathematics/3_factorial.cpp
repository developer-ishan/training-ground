#include<bits/stdc++.h>
using namespace std;

int fact(int n){
  if(n<=2)
    return n;
  return n*fact(n-1);
}
int main(int argc, char const *argv[])
{
  int n;
  cin>>n;
  cout << "Recursive factorial: " << fact(n) <<"\n";


  int fact = 1;
  for(int i=1; i<=n; i++){
    fact = fact * i;
  }
  cout << "Iterative factorial: " << fact << "\n";
  return 0;
}
