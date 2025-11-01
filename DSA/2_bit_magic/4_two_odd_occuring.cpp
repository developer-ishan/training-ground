#include<bits/stdc++.h>
using namespace std;

void oddAppearing(vector<int> arr, int n){
  int x = arr[0];
  for(int i=1; i<n; i++){
    x^=arr[i];
  }

  int k = x & ~(x-1); //last set bit of x
  int grp1 = 0, grp2 = 0;
  for(int i=0; i<n; i++){
    if((arr[i] & k) == 0)
      grp1^=arr[i];
    else 
      grp2^=arr[i];
  }

  cout<<grp1<<" "<<grp2<<"\n";
}
int main(int argc, char const *argv[]){
    oddAppearing({1,2,3,3,4,4}, 6);
  return 0;
}