#include<bits/stdc++.h>
using namespace std;

void print_power_set(string s){
  int n = s.length();
  int pSize = 1<<n;
  for(int i=0; i<pSize; i++){
    for(int j=0; j<n; j++){
      if((i & (1<<j)) != 0){
        cout<<s[j];
      }
    }
    cout<<"\n";
  }
}
int main(int argc, char const *argv[]){
  string s = "abc";
  print_power_set(s);
  return 0;
}